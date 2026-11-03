#/bin/sh

adb=~/Android/Sdk/platform-tools/./adb

files="$(${adb} shell 'run-as com.example.iBrush ls /data/data/com.example.iBrush/files/')"

for filename in ${files}; do
    command="${adb} shell \"run-as com.example.iBrush cat /data/data/com.example.iBrush/files/$filename\" > $filename"
    eval "$command"
    done
