#include <iostream>
#include <fstream>
#include <cstdlib>
#include <vector>
#include <algorithm>
#include <ctime>
using namespace std;

#define NUM_FREQS 2600
#define TOTAL_FREQS 10000

int main()
{
    srand(time(NULL));

    vector<int> freqs;
    for (int i = 0; i < TOTAL_FREQS; i++)
        freqs.push_back(i);

    random_shuffle(freqs.begin(), freqs.end());

    ofstream outputfile("freqtable.txt");
    outputfile << "\tint[] freqtable = {";

    for (int i = 0; i < NUM_FREQS; i++)
    {
        if (i % 50 == 0)
            outputfile << "\n\t\t";

        outputfile << freqs[i] << ", ";
    }
    outputfile << "\n";
    outputfile << "\t};";
    outputfile.close();

    return 0;
}
