# modified-SPICE-score

This repo is a modified version of [Semantic Propositional Image Caption Evaluation (SPICE)](https://github.com/peteanderson80/SPICE), which is designed for experiments in our paper [FACTUAL: A Benchmark for Faithful and Consistent Textual Scene Graph Parsing](https://arxiv.org/pdf/2305.17497.pdf) (ACL 2023).

You can obtain following result file using this code:

1. Scece graphs of SPICE parser (Stanford scene graph parser).

2. SPICE score of customized scene graphs.

Note: this implementation is easy and *extremely inelegant*.

## Usage

The requirements and dependencies are the same with [SPICE](https://github.com/peteanderson80/SPICE).

`.\src\test\java\edu\anu\spice\SpiceTest.java` and `.\src\main\java\edu\anu\spice\SpiceScorer.java` have been modified according to our requirements.
All you need to do is to simplely run `SpiceTest.java`.

### SPICE parser results

Firstly, you need to process your data in the same format as `example_input.json`, and then change the `inFile` to your data file name in `SpiceTest.java`.

Then, you can refer to line 146 in `SpiceScorer.java` to save the parsed scene graphs to txt file.

### SPICE score

You can refer to the class `testTupleSet` in `SpiceScorer.java` file to transfer your own scene graphs to SceneGraph class in SPICE, which can be used to compute SPICE score. 
In this case, the actual scene graph inputs is your own file which would be processed in the class `testTupleSet`.

## Results

After running `SpiceTest.java`, a txt file or SPICE score json file would be saved.
We provide the SPICE score json file we used for results in Table 5 of our paper in `results` folder, and you can obtain the exactly same results using these files.
The test code is available at [here](https://github.com/jmhessel/clipscore/blob/main/flickr8k_example/compute_metrics.py).
