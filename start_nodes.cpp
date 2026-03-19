#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>
#include <thread>

using namespace std;

vector<string> create_commands() {
    ifstream file("network.config");
    vector<string> commands;
    string line;

    while (getline(file, line)) {
        
        stringstream ss(line);
        string token;
        string command = "java Server_run_instance";

        int i = 0;
        while (getline(ss, token, ',') && i < 3) {
            if (i == 0 || i == 2)
                command += " " + token;

            i++;
        }

        commands.push_back(command);
    }

    file.close();
    return commands;
}

void run_command(string command)
{
    system(command.c_str());
}

int main() {
    vector<string> commands = create_commands();
    std::vector<std::thread*> threads;

    for (int i = 0; i < commands.size(); i++)
    {
        std::thread* t = new std::thread(run_command, commands[i]);
        threads.push_back(t);
    }

    for (int i = 0; i < commands.size(); i++)
    {
        threads[i]->join();
    }
}