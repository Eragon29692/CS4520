#!/bin/python

f = open('wordlist.txt', 'r')
w = open('wordlistProcessed.txt', 'a')
i = 0
temp = ''
beginChar = 'a'
for line in f:
    if (beginChar == line[0]):
        if (i == 40):
            w.write(temp + '\n')
            temp = ''
            i = 0
        if (temp == ''):
            temp = line[:len(line) - 1]
        else:
            temp = temp + ',' + line[:len(line) - 1]
        i = i + 1
    else:
        if (temp != ''):
            w.write(temp + '\n')
        temp = line[:len(line) - 1]
        i = 0
        beginChar = line[0]

w.write('last ' + temp)

print 'done'