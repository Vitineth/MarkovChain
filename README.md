# MarkovChain
A rewrite of the Markov-Chains repository with a new working project. This one is better than the last and will be replacing the respository.

## What this is
This is a java implementation of the Markov Chains program which is defined as 
> a stochastic model describing a sequence of possible events in which the probability of each event depends only on the state attained in the previous event.
This means that each value generatd is based off the probability that it follows the previous value. 

## What does this do
This version generates sentences based on a provided piece of text. It splits it into parts and determines how likely each part is to follow the last and will then generate a sentence based off that data. In this example there is also the option to generate sentences based on the sentence structures that exist within the text. 

## How to run
The program has 4 command line options:

| Flag     | Argument type | Description |
| -------- | ------------- | ---------- |
| -file    | String        | The input file location |
| -regular | Integer       | The number of regular sentences to generate from the input data |
| -english | Integer       | The number of english structured sentences to generate from the input data |
| -timings | Boolean       | Whether the timings should be outputted once its completed |

It can be executed like so
> ```java -jar markov.jar -file [file] -regular [n] -english [n] -timings [true/false]```

Asssuming ```markov.jar``` is the name of this jar file
