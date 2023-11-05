#!/bin/bash

# Check if the number of tabs is provided as an argument
if [ $# -ne 2 ]; then
  echo "Usage: $0 <number_of_processes> <running_mode> (demo | example)"
  exit 1
fi

# Extract the number of tabs from the first argument
num_tabs="$1"
running_mode="$2"

# Specify the folder path
folder_path="/home/aleister/Repos/ses-algorithm"

# Loop through and open tabs
for ((i = 0; i <= num_tabs; i++)); do
  # Calculate the command to be executed
  command="./gradlew process${i} -Prunning_mode=${running_mode}"

  # Set the tab's name
  tab_name="Process${i}"

  # Open a new tab with the specified working directory and command, setting the name
  gnome-terminal --tab --working-directory="$folder_path" --title="Process${i}" -- zsh -c "$command; exec zsh"
done

