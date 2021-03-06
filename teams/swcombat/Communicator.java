package swcombat;

/*************************************
Communicator.java - Communications interface
Encapsulates all communications-related functions.

Each robot has a unique offset in the frequency table that depends on the current round number.
The [maxMsgQueueLen] consecutive channels starting at that offset is that robot's "inbox".
This implements a naive frequency-hopping scheme.

Stickies are in a global messagespace called a stickyspace. I like sticky things.
This stickyspace can be accessed by posting to id = -1.

Each channel contains a 32-bit packet, containing:
|Data byte 3| |Data byte 2| |Data byte 1| |Checksum + timestamp byte|
where the checksum byte is calculated by xoring the data bytes with the current round number.

Example (for posting to stickyspace):
comm.receive(comm.IDtoCurFreq(-1)) receives data sent to the current round.
comm.send(comm.IDtoNextFreq(-1), data, 3) sends 3 bytes of data to the next round.
comm.send(comm.IDtoFreqNRoundsLater(-1, 90), data, 3) sends 3 bytes of data to 90 rounds later.
*************************************/

import battlecode.common.*;
import java.util.Arrays;

public class Communicator
{
	private BaseRobot r;
	
	// These constansts have been carefully chosen, please do not change!
	private static final int maxMsgQueueLen = 64;
		// measured in number of channels, e.g. 4 len = 12 chars = 96 bits
		// DO NOT SET THIS TO LARGER THAN 64, OTHERWISE CHANNELS WILL OVERLAP.
	private static final int maxNewMsgsPerRound = 17;
		// [maximum number of new messages] + 1 that a robot will receive per round.
		// Should be smaller than maxMsgQueueLen / 2
		
	private static final char magicChecksumVal = (char)0xB1;
	
	Communicator(BaseRobot robot)
	{
		this.r = robot;
	}
	
	// Posts data to the message queue starting at freqtable[offset]
	// e.g. send(2, "hi", 2) will post \x68\x69 to channel 8175
	// If datalen > 2, remaining bytes will be written to successive freqtable channels
	// e.g. send(2, "hello", 5) will write to channels 8175, 1867, 5264.
	// The set of messages beginning at 8175 is the "message queue starting at 8175".
	public void send(int freq, char[] data, int datalen) throws GameActionException
	{
		int packet;
		char c1, c2, c3;
		for (int i = freq, c = 0; c < datalen; i = (i + 1) % GameConstants.BROADCAST_MAX_CHANNELS, c+=3)
		{
			// Find the next open spot in the message queue for a packet
			while(isValidPacket(r.rc.readBroadcast(i)))
				i = (i + 1) % GameConstants.BROADCAST_MAX_CHANNELS;
				
			c1 = data[c];
			c2 = (c+1 >= datalen) ? 0xAA : data[c+1];	// do not change these padding values!
			c3 = (c+2 >= datalen) ? 0x55 : data[c+2];	// do not change these padding values!
			
			// Three data bytes (with MSB last to speed up receiving)
			packet = (c3 << 24) | (c2 << 16) | (c1 << 8);
			// Add a checksum as the last byte
			packet |= c1 ^ c2 ^ c3 ^ ((r.curRound + 1) & 0xFF) ^ magicChecksumVal;
			r.rc.broadcast(i, packet);
		}
	}
	
	// Reads all valid messages in the message queue starting at channel freqtable[offset]
	// Returns maxMsgQueueLen messages as an array of 3 * maxMsgQueueLen chars.
	public char[] receive(int freq) throws GameActionException
	{
		int packet, i = 0;
		char[] data = new char [3 * maxMsgQueueLen];

		while(isValidPacket(packet = r.rc.readBroadcast(freq)))
		{
			packet >>= 8;	// First character (discarding checksum)
			data[i++] = (char)(packet & 0xFF);

			packet >>= 8;	// Second character
			data[i++] = (char)(packet & 0xFF);
			
			packet >>= 8;	// Third character
			data[i++] = (char)(packet & 0xFF);

			freq++;
			if (i == 3 * maxMsgQueueLen) break;
		}
		return Arrays.copyOfRange(data, 0, i+1);
	}
	
	boolean isValidPacket(int packet)
	{
		//Efficiency note: some of this casting is probably unnecessary
		char checksum = (char) (packet & 0xFF);
		checksum ^= (char)((packet & 0xFF000000) >> 24);
		checksum ^= (char)((packet & 0x00FF0000) >> 16);
		checksum ^= (char)((packet & 0x0000FF00) >> 8);
		checksum ^= (char)(r.curRound & 0xFF);
		return (checksum == magicChecksumVal);
	}
	
	public int IDtoCurFreq(int id)
	{
		return IDtoFreqNRoundsLater(id, 0);
	}
	public int IDtoNextFreq(int id)
	{
		return IDtoFreqNRoundsLater(id, 1);
	}
	public int IDtoFreqNRoundsLater(int id, int roundOffset)
	{
		int tmp = maxMsgQueueLen * id - maxNewMsgsPerRound * (r.curRound + roundOffset);
		if (tmp < 0) tmp += GameConstants.BROADCAST_MAX_CHANNELS;
		return tmp % GameConstants.BROADCAST_MAX_CHANNELS;
	}
}