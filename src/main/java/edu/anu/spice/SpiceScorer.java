/*
 * Copyright (c) 2016, Peter Anderson <peter.anderson@anu.edu.au>
 *
 * This file is part of Semantic Propositional Image Caption Evaluation
 * (SPICE).
 * 
 * SPICE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * SPICE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public
 * License along with SPICE.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package edu.anu.spice;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import edu.cmu.meteor.aligner.SynonymDictionary;
import edu.cmu.meteor.util.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.base.Stopwatch;

import static org.junit.Assert.assertTrue;

public class SpiceScorer {
	
	public SpiceStats stats;
	
	SpiceScorer(){
		stats = null;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			SpiceArguments.printUsage();
			System.exit(2);
		}
		SpiceArguments spiceArgs = new SpiceArguments(args);
		try {
			SpiceScorer scorer = new SpiceScorer();
			scorer.scoreBatch(spiceArgs);
		} catch (Exception ex) {
			System.err.println("Error: Could not score batched file input:");
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public void scoreBatch(SpiceArguments args) throws IOException, ScriptException {
		Stopwatch timer = Stopwatch.createStarted();
		SpiceParser parser = new SpiceParser(args.cache, args.numThreads, args.synsets);
		
		// Build filters for tuple categories
		Map<String, TupleFilter> filters = new HashMap<String, TupleFilter>();
		if (args.tupleSubsets) {
			filters.put("Object", TupleFilter.objectFilter);
			filters.put("Attribute", TupleFilter.attributeFilter);
			filters.put("Relation", TupleFilter.relationFilter);
			filters.put("Cardinality", TupleFilter.cardinalityFilter);
			filters.put("Color", TupleFilter.colorFilter);
			filters.put("Size", TupleFilter.sizeFilter);
		}
		
		// Parse test and refs from input file
		ArrayList<Object> image_ids = new ArrayList<Object>();
		ArrayList<String> testCaptions = new ArrayList<String>();
		ArrayList<String> refCaptions = new ArrayList<String>();
		ArrayList<Integer> refChunks = new ArrayList<Integer>();
		ArrayList<String> refsStrings = new ArrayList<String>();
		ArrayList<JSONArray> all_refs = new ArrayList<JSONArray>();
		ArrayList<String> testCaptions2 = new ArrayList<String>();
		JSONParser json = new JSONParser();
		JSONArray input1;
		JSONArray input2;
		try {
			input1 = (JSONArray) json.parse(new FileReader(args.inputPath));
			for (Object o : input1) {
			    JSONObject item = (JSONObject) o;
//			    image_ids.add(item.get("image_id"));
			    testCaptions.add((String) item.get("test"));
//			    JSONArray refs = (JSONArray) item.get("refs");
//				all_refs.add(refs);
//				System.out.println(refs.toString());
//				refsStrings.add(refs.toString());
//				refCaptions.add((String) item.get("refs"));
//			    refChunks.add(refs.size());
//			    for (Object ref : refs){
//			    	refCaptions.add((String) ref);
//			    }
			}
//			input2 = (JSONArray) json.parse(new FileReader("src/test/data/new_spice_norepeat1.json"));
//			for (Object o2 : input2) {
//				JSONObject item = (JSONObject) o2;
//				testCaptions2.add((String) item.get("test"));
//				JSONArray refs = (JSONArray) item.get("refs");
//				all_refs.add(refs);
//			}

		} catch (ParseException e) {
			System.err.println("Could not read input: " + args.inputPath);
			System.err.println(e.toString());
			e.printStackTrace();
		}
		
		System.err.println("Parsing reference captions");
		List<SceneGraph> refSgs = parser.parseCaptions(refCaptions, refChunks);
//		List<SceneGraph> refSgs = testTupleSet(refCaptions);
//		List<SceneGraph> refSgs = parser.parseCaptions(refCaptions);
		System.err.println("Parsing test captions");
		List<SceneGraph> testSgs = parser.parseCaptions(testCaptions);
//		List<SceneGraph> testSgs = testTupleSet(testCaptions);
//		System.out.println(testSgs.get(0).toReadableString());
//		System.out.println(refSgs.size());
//		System.out.println(testSgs.size());

		try {
			File file = new File("spice_test_2_captions.txt");
			PrintStream ps1 = new PrintStream(new FileOutputStream(file));
			for (int i=0; i<testCaptions.size(); ++i) {
//				this.stats.score(image_ids.get(i), testSgs.get(i), refSgs.get(i), args.synsets);
//				ps1.println(testCaptions2.get(i));
				ps1.println(testSgs.get(i).toReadableString());
//				ps1.println(all_refs.get(i));
//				ps1.println(refSgs.get(i).toReadableString());
				ps1.println("*****************************************");
//				ps1.println("\n");
			}

//			File file2 = new File("refs_out_v2.txt");
//			PrintStream ps2 = new PrintStream(new FileOutputStream(file2));
//			for (int j=0; j<testCaptions.size(); ++j) {
////				this.stats.score(image_ids.get(i), testSgs.get(i), refSgs.get(i), args.synsets);
//				ps2.println(refCaptions.get(j));
//				ps2.println(refSgs.get(j).toReadableString());
//				ps2.println("\n");
//			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.stats = new SpiceStats(filters, args.detailed);
		for (int i=0; i<testSgs.size(); ++i) {
			this.stats.score(image_ids.get(i), testSgs.get(i), refSgs.get(i), args.synsets);
		}
		if (!args.silent){
			System.out.println(this.stats.toString());
		}
		
		if (args.outputPath != null) {
			BufferedWriter outputWriter = new BufferedWriter(new FileWriter(args.outputPath));
			
			// Pretty print output using javascript
			String jsonStringNoWhitespace = this.stats.toJSONString();
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
			scriptEngine.put("jsonString", jsonStringNoWhitespace);
			scriptEngine.eval("result = JSON.stringify(JSON.parse(jsonString), null, 2)");
			String prettyPrintedJson = (String) scriptEngine.get("result");
			
			outputWriter.write(prettyPrintedJson);			
			outputWriter.close();
		}
		System.out.println("SPICE evaluation took: " + timer.stop());
	}

	public List<SceneGraph> testTupleSet(ArrayList<String> input) {

		SynonymDictionary synonyms;
		URL synDirURL = Constants.DEFAULT_SYN_DIR_URL;
		try {
			URL excFileURL = new URL(synDirURL.toString() + "/english.exceptions");
			URL synFileURL = new URL(synDirURL.toString() + "/english.synsets");
			URL relFileURL = new URL(synDirURL.toString() + "/english.relations");
			synonyms = new SynonymDictionary(excFileURL, synFileURL, relFileURL);
		} catch (IOException ex) {
			throw new RuntimeException("Error: Synonym dictionary could not be loaded (" + synDirURL.toString() + ")");
		}
		ArrayList<SceneGraph> all = new ArrayList<SceneGraph>();
//		boolean[] allowMerge = {true,false};
		for (int i=0; i<input.size(); i++){
			SceneGraph can_sg = new SceneGraph(synonyms, true);
			String tups = input.get(i).replace(" ) ,", "");
			String[] all_tup = tups.split(",");
			for (String t: all_tup) {
				String[] words = t.split("\\+");
				if (words.length < 3) {
//					System.out.println(t);
//					System.out.println(tups);
					can_sg.addObject(words[0]);
					continue;
				}

				if (words[1].equals("is")) {
					can_sg.addObject(words[0]);
//					can_sg.addAttribute(words[0], words[2]);
//					can_sg.addObject(words[2]);
					can_sg.addAttribute(words[0], words[2]);
				} else {
					can_sg.addObject(words[0]);
					can_sg.addObject(words[2]);
					can_sg.addRelation(words[0], words[2], words[1]);
				}
			}
			all.add(can_sg);
		}
//			can_sg.addObject("bus");
//			can_sg.addAttribute("bus", "red");
//			can_sg.addRelation("bus", "street", "on");
//			can_sg.addAttribute("bus", "double-decker");
//			can_sg.addObject("street");
//			can_sg.addObject("bus");
//			can_sg.addAttribute("bus", "double-decker");
//			can_sg.addRelation("bus", "street", "on");
//			can_sg.addObject("bus");
			//System.out.println(can_sg.toReadableString());
//			TupleSet can = new TupleSet(can_sg);
//
//			SceneGraph ref_sg = new SceneGraph(synonyms, allowMerge[i]);
//			ref_sg.addObject("bus");
//			ref_sg.addAttribute("bus", "red");
//			ref_sg.addAttribute("bus", "double-decker");
//			ref_sg.addObject("street");
//			ref_sg.addRelation("bus", "street", "on");
//			//System.out.println(ref_sg.toReadableString());
//			TupleSet ref = new TupleSet(ref_sg);
//
//			TupleSet.Count count = can.match_exact(ref);
//			assertTrue(count.n == can.size());
//			assertTrue(count.n == 5);

		return all;
	}
}
