#include <iostream>
#include <fstream>
#include <cstdlib>
#include <vector>
#include <algorithm>
#include <ctime>
using namespace std;

#define NUM_FREQS 16000
#define TOTAL_FREQS 65535

int main()
{
    srand(time(NULL));

    vector<int> freqs;
    for (int i = 0; i < TOTAL_FREQS; i++)
        freqs.push_back(i);

    random_shuffle(freqs.begin(), freqs.end());

    ofstream outputfile("freqtable.txt");
    outputfile << "\tprivate static final int freqtable_size = " << NUM_FREQS << ";\n";
    outputfile << "\tprivate static int[] freqtable = {";

    for (int i = 0; i < NUM_FREQS; i++)
    {
        if (i % 100 == 0)
            outputfile << "\n\t\t";

        outputfile << freqs[i] << ", ";
    }
    outputfile << "\n";
    outputfile << "\t};\n";

    outputfile << "\tprivate static final int stickyfreqtable = {";
    for (int i = 0; i < 2500; i++)
    {
        if (i % 100 == 0)
            outputfile << "\n\t\t";

        outputfile << freqs[NUM_FREQS + i] << ", ";
    }
    outputfile << "\n";
    outputfile << "\t};";

    outputfile.close();

    return 0;
}
