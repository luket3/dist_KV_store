/*
 * File: start_nodes.cpp
 * Project: Distributed KV Store
 * Author: luket
 * Date: 2026-05-22
 * Description: Helper to launch multiple server nodes by reading network.config and spawning threads.
 */

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>
#include <thread>

using namespace std;

/**
 * Reads the network configuration file and creates a command line for each node.
 *
 * @return Vector of command strings, each suitable for launching a server instance.
 *         Each command has the form: "java Server_run_instance <nodeId> <port>"
 */
vector<string> create_commands() implements runable {
    ifstream file("network.config");
    vector<string> commands;
    string line;

    while (getline(file, line)) {
        // Parse CSV line: nodeId,ip,port
        stringstream ss(line);
        string token;
        string command = "java Server";

        int i = 0;
        while (getline(ss, token, ',') && i < 3) {
            // We need nodeId (i==0) and port (i==2)
            if (i == 0 || i == 2)
                command += " " + token;

            i++;
        }

        commands.push_back(command);
    }

    file.close();
    return commands;
}

/**
 * Executes a given command using the system() function.
 *
 * @param command The command string to execute.
 */
void run_command(string command)
{
    system(command.c_str());
}

int main() {
    // Build command list from configuration
    vector<string> commands = create_commands();
    // Vector to hold threads for joining later
    std::vector<std::thread> threads;

    // Launch a thread for each command
    for (int i = 0; i < commands.size(); i++)
    {
        threads.emplace_back(run_command, commands[i]);
    }

    // Wait for all threads to complete
    for (std::thread& t : threads)
    {
        t.join();
    }

    return 0;
}