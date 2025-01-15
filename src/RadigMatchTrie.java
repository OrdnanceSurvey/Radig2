/**
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 *
 * version 1.0
 *
 * Date: 2025-01-15
 *
 * Copyright (C) 2025 Ordnance Survey
 *
 * Licensed under the Open Government Licence v3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 *   
 *  
 * 
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * 
 * version 0.1
 * 
 * Date: 2022-09-28
 * 
 * Copyright (C) 2022 Ordnance Survey
 */
package uk.osgb.algorithm.radig2;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import uk.osgb.datastructures.MultiTreeMap;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

public class RadigMatchTrie {
	// the digit stored at this trie node
	Character digit = null;
	// flag for traversal
	Character lastVisitedChild = null;
	// point to parent node, for efficient traversal
	RadigMatchTrie parent = null;
	// child node, with next character as key
	TreeMap<Character, RadigMatchTrie> children = null;
	// features with key terminated at this node
	LinkedList<Object> features = null;
	// flag for traversal
	boolean isVisited = false;
	//
	/**
	 * @param parent
	 */
	private RadigMatchTrie(RadigMatchTrie parent) {
		this.parent = parent;
	}
	//
	/**
	 * @return
	 */
	public static RadigMatchTrie createRadigMatchTrie() {
		return new RadigMatchTrie(null);
	}

	//
	private boolean isTerminal() {
		return features!=null;
	}

	/** return the child with key d
	 * @param d
	 * @return
	 */
	private RadigMatchTrie getChild(Character d) {
		if(children!=null && d!=null) {
			return children.get(d);
		}else {
			return null;
		}
	}
	//

	/**
	 * @param d
	 * @return
	 */
	private RadigMatchTrie getNextChild(Character d) {
		if(children!=null) {
			if(d==null) {
				return children.firstEntry().getValue();
			}else {
				Entry<Character, RadigMatchTrie> entry = children.higherEntry(d);
				if(entry!=null) {
					return entry.getValue();
				}else {
					return null;
				}
			}
		}else {
			return null;
		}
	}
	//

	/** entrance node for a trie, to insert a reference into the trie
	 * @param radigRef
	 */
	public void insertRadigRef(String radigRef, Object feature) {
		if(parent == null) {// root
			char d = radigRef.charAt(0);
			if(children == null) {
				children = new TreeMap<Character, RadigMatchTrie>();
			}
			RadigMatchTrie topNode = children.get(d);
			if(topNode == null) {
				topNode = new RadigMatchTrie(this);
				children.put(d, topNode);
			}
			topNode.insertRadigRef(radigRef, 0, feature);
		}
	}

	/** set the digit of the trie at idx and insert the remainder of the ref to child tries
	 * @param radigRef
	 * @param idx index of the character in radigRef
	 */
	private void insertRadigRef(String radigRef, int idx, Object feature) {
		digit = radigRef.charAt(idx);
		if(idx == radigRef.length()-1) {// last digit in string, terminate at here
			if(features==null) {
				features = new LinkedList<Object>();
			}
			features.add(feature);
		}else {
			idx+=1;
			char digitNext = radigRef.charAt(idx);
			if(children==null) {
				children = new TreeMap<Character, RadigMatchTrie>();
			}
			RadigMatchTrie child = children.get(digitNext);
			if(child==null) {// not in children yet
				child = new RadigMatchTrie(this);
				children.put(digitNext, child);
			}
			child.insertRadigRef(radigRef, idx, feature);
		}
	}

	/**
	 * @param radigRef
	 * @param exactMatch
	 * @param matchedRefs
	 * @return
	 */
	public void queryRadigRef(String radigRef, boolean exactMatch, List<String> matchedRefs) {
		RadigMatchTrie node = this;
		for(int i = 0; i < radigRef.length();++i) {
			node = node.getChild(radigRef.charAt(i));
			if(node == null) {
				return;
			}
		}
		if(exactMatch && node.isTerminal()) {// there is a key in this Trie terminates here
			matchedRefs.add(node.getPrefix()+node.digit);
		}else {
			if(!exactMatch) {
				//getAllRefs(node.getPrefix(), matchedRefs);
				node.getAllRefs(node.getPrefix(), matchedRefs);
			}
		}
	}

	public void resetLastVisitedChildFlag() {
		lastVisitedChild = null;
		isVisited = false;
		if(children!=null) {
			for(RadigMatchTrie child:children.values()) {
				child.resetLastVisitedChildFlag();
			}
		}
	}
	//

	/** get prefix formed by digit from the top node to this one (excluding digit on this)
	 * @return
	 */
	private String getPrefix() {
		String prefix = "";
		RadigMatchTrie node = this.parent;
		while(node!=null && node.digit!=null) {
			prefix = node.digit+prefix;
			node = node.parent;
		}
		return prefix;
	}
	//

	/** get all references with current node as an ancestor (if current node is terminal, return it as well) - do we need it?
	 * @param prefix
	 * @param matchedRefs
	 */
	private void getAllRefs(String prefix, List<String> matchedRefs) {
		if(prefix == null) {
			if(this.digit !=null) {
				prefix = "" + this.digit;
			}else{
				prefix = "";
			}
		}else{
			if(this.digit!=null) {
				prefix = prefix + this.digit;
			}
		}
		if(this.isTerminal()) {
			matchedRefs.add(prefix);
		}
		if(children!=null) {
			for(RadigMatchTrie child:children.values()) {
				child.getAllRefs(prefix, matchedRefs);
			}
		}
	}

	/** return all valid references on the node or in children that matches keyRef, and user keyRef as key to add to returning multiMap
	 * @param prefix
	 * @param keyRef
	 * @param matchedRefs
	 */
	private void getAllMatchedRefs(String prefix, String keyRef, boolean equalOnly, boolean containOnly, MultiTreeMap<String, String> matchedRefs) {
		String refOnThis = prefix + this.digit;
		if(this.isTerminal()) {
			if(!containOnly) {
				matchedRefs.put(keyRef, refOnThis);
			}
		}
		if(children!=null && !equalOnly) {
			for(RadigMatchTrie child:children.values()) {
				child.getAllMatchedRefs(refOnThis, keyRef, false, false, matchedRefs);
			}
		}
	}
	//
	//

	/** join two RadigTries. return a multimap contain references in this trie as keys, and matched (those starting with the key) references in other as values.
	 *
	 *  To obtain a full set of intersection of two RadigTrie, run this.traverseMath(other, false, false, rlt1) and then other.traverseMath(this, false, true, rlt2)
	 *  and then merge rlt1 and rlt2
	 *
	 * @param other the other RadigTrie to be matched to this
	 * @param equalOnly if true, exact reference match only; equalOnly and containOnly can't both be true
	 * @param containOnly if true, match contained references in other only; equalOnly and containOnly can't both be true
	 * @param matchedRefs
	 */
	public void traverseReferenceMatch(RadigMatchTrie other, boolean equalOnly, boolean containOnly, MultiTreeMap<String, String> matchedRefs) {
		if(equalOnly&&containOnly) {
			System.out.println("equalOnly and containOnly can't be both TRUE...");
		}
		RadigMatchTrie parentThis = this;
		RadigMatchTrie parentThat = other;
		while(true) {
			//System.out.println(parentThis.getPrefix());
			if(parentThis.isTerminal() && !isVisited) {// a key terminates at this node but there may be more keys as its children
				String prefix = parentThis.getPrefix();
				String keyRef = prefix+parentThis.digit; // reference on this node
				if(!matchedRefs.containsKey(keyRef)) {    // check if it has been retrieved, if not, get matching references
					parentThat.getAllMatchedRefs(prefix, keyRef, equalOnly, containOnly, matchedRefs);
				}
				isVisited = true;
			}
			RadigMatchTrie childThis = parentThis.getNextChild(parentThis.lastVisitedChild);
			if(childThis !=null) {
				parentThis.lastVisitedChild = childThis.digit;
				RadigMatchTrie childThat = parentThat.getChild(childThis.digit);
				if(childThat==null) { // no matching in this branch
					// move to next child of parentThis
//					parentThis = parentThis.parent;
//					parentThat = parentThat.parent;
				}else {// drill down
					parentThis = childThis;
					parentThat = childThat;
				}
			}else {// no more child
				if(parentThis == this) {// back tracked to the root, all top child nodes are visited
					break;
				}else {
					// backtrack
					parentThis = parentThis.parent;
					parentThat = parentThat.parent;
				}
			}
		}
	}
/********
 *
 *
 *
 ********/
	/**
	 * @param prefix prefix for the current node (not including current node's digit)
	 * @param keyRef the key to be used in returning matched features
	 * @param equalOnly
	 * @param containOnly
	 * @param matchedFeatures
	 */
	private void getAllMatchedFeatures(String prefix, String keyRef, boolean equalOnly, boolean containOnly, MultiTreeMap<String, Object> matchedFeatures) {
		String refOnThis = prefix + this.digit;
		if(this.isTerminal()) {
			if(!containOnly) {
				matchedFeatures.putAll(keyRef, this.features);
			}
		}
		if(children!=null && !equalOnly) {
			for(RadigMatchTrie child:children.values()) {
				child.getAllMatchedFeatures(refOnThis, keyRef, false, false, matchedFeatures);
			}
		}
	}
	//

	/** find matched features in other for each feature in this
	 * @param other
	 * @param equalOnly
	 * @param containOnly
	 * @param matchedFeatures
	 */
	public void traverseFeatureMatch(RadigMatchTrie other, boolean equalOnly, boolean containOnly, MultiTreeMap<Object, Object> matchedFeatures) {
		if(equalOnly&&containOnly) {
			System.out.println("equalOnly and containOnly can't be both TRUE...");
		}
		// store key-features of visited terminal in this
		MultiTreeMap<String, Object> mapThis = new MultiTreeMap<String, Object>();
		// store matched key-features in other
		MultiTreeMap<String, Object> mapThat = new MultiTreeMap<String, Object>();
		// starts at roots of the two tries
		RadigMatchTrie parentThis = this;
		RadigMatchTrie parentThat = other;
		while(true) {
			System.out.println(parentThis.getPrefix() + " --- "+parentThat.getPrefix());
			if(parentThis.isTerminal() && !isVisited) {
				String prefix = parentThis.getPrefix();
				String keyRef = prefix+parentThis.digit;
				if(!mapThis.containsKey(keyRef)) {    // check if it has been retrieved, if not, get matching references
					mapThis.putAll(keyRef, parentThis.features);
					parentThat.getAllMatchedFeatures(prefix, keyRef, equalOnly, containOnly, mapThat);
				}
				isVisited = true;
			}
			RadigMatchTrie childThis = parentThis.getNextChild(parentThis.lastVisitedChild);
			if(childThis !=null) {
				parentThis.lastVisitedChild = childThis.digit;
				RadigMatchTrie childThat = parentThat.getChild(childThis.digit);
				if(childThat==null) { // no matching in this branch
					// move to next child, if any
					//parentThis = parentThis.parent;
					//parentThat = parentThat.parent;
				}else {// drill down
					parentThis = childThis;
					parentThat = childThat;
				}
			}else {// no more child
				if(parentThis == this) {// back tracked to the root, all top child nodes are visited
					break;
				}else {
					// backtrack
					parentThis = parentThis.parent;
					parentThat = parentThat.parent;
				}
			}
		}
		for(String key:mapThis.keySet()) {
			Collection<Object> featuresThis = mapThis.get(key);
			Collection<Object> featuresThat = mapThat.get(key);
			for(Object feat:featuresThis) {
				matchedFeatures.putAll(feat, featuresThat);
			}
		}
	}

	/** find matched features in other for each feature in this and return a list of matched pairs (using Map.Entry<K, V>)
	 * @param other
	 * @param equalOnly
	 * @param containOnly
	 * @return
	 */
	public List<Map.Entry<Object, Object>> traverseFeatureMatchFlat(RadigMatchTrie other, boolean equalOnly, boolean containOnly) {
		if(equalOnly&&containOnly) {
			System.out.println("equalOnly and containOnly can't be both TRUE...");
		}
		// store key-features of visited terminal in this
		MultiTreeMap<String, Object> mapThis = new MultiTreeMap<String, Object>();
		// store matched key-features in other
		MultiTreeMap<String, Object> mapThat = new MultiTreeMap<String,Object>();
		//
		RadigMatchTrie parentThis = this;
		RadigMatchTrie parentThat = other;
		while(true) {
//			System.out.println(parentThis.getPrefix() + "-" + parentThat.getPrefix());
			if(parentThis.isTerminal() && !isVisited) {
				String prefix = parentThis.getPrefix();
				String keyRef = prefix+parentThis.digit;
				if(!mapThis.containsKey(keyRef)) {    // check if it has been retrieved, if not, get matching references
					mapThis.putAll(keyRef, parentThis.features);
					parentThat.getAllMatchedFeatures(prefix, keyRef, equalOnly, containOnly, mapThat);
				}
				isVisited = true;
			}
			RadigMatchTrie childThis = parentThis.getNextChild(parentThis.lastVisitedChild);
			if(childThis !=null) {
				parentThis.lastVisitedChild = childThis.digit;
				RadigMatchTrie childThat = parentThat.getChild(childThis.digit);
				if(childThat==null) { // no matching in this branch
					// move to next child of parentThis, if any
//					parentThis = parentThis.parent;
//					parentThat = parentThat.parent;
				}else {// drill down
					parentThis = childThis;
					parentThat = childThat;
				}
			}else {// no more child
				if(parentThis == this) {// back tracked to the root, all top child nodes are visited
					break;
				}else {
					// backtrack
					parentThis = parentThis.parent;
					parentThat = parentThat.parent;
				}
			}
		}
		List<Map.Entry<Object, Object>> matchedFeaturePairs = new LinkedList<Map.Entry<Object, Object> >();
		for(String key:mapThis.keySet()) {
			Collection<Object> featuresThis = mapThis.get(key);
			Collection<Object> featuresThat = mapThat.get(key);
			for(Object featThis:featuresThis) {
				for(Object featThat:featuresThat) {
					matchedFeaturePairs.add(new AbstractMap.SimpleEntry<Object, Object>(featThis, featThat));
				}

			}
		}
		return matchedFeaturePairs;
	}

	/** Join this and other, first match this to other, then match other to this, finally merge the results and return via the multimap keyed by features in this.
	 * @param other
	 * @param equalOnly
	 * @param containOnly
	 * @param matchedFeatures
	 */
	public void traverseFeatureJoin(RadigMatchTrie other, boolean equalOnly, boolean containOnly, MultiTreeMap<Object, Object> matchedFeatures) {
		this.traverseFeatureMatch(other, equalOnly, containOnly, matchedFeatures);
		this.resetLastVisitedChildFlag();
		other.resetLastVisitedChildFlag();
		MultiTreeMap<Object, Object> otherToThis = new MultiTreeMap<Object, Object>();
		other.traverseFeatureMatch(this, equalOnly, containOnly, otherToThis);

		for(Object valOther:otherToThis.keySet()) {
			Collection<Object> keysThis = otherToThis.get(valOther);
			for(Object keyThis:keysThis) {
				matchedFeatures.put(keyThis, valOther);
			}
		}
	}

	public Set<Map.Entry<Object, Object>> traverseFeatureJoinFlat(RadigMatchTrie other, boolean equalOnly, boolean containOnly){
		MultiTreeMap<Object, Object> matchedFeatures = new MultiTreeMap<Object, Object>();
		this.traverseFeatureJoin(other, equalOnly, containOnly, matchedFeatures);
		return matchedFeatures.entrySet();
	}

	public Set<Map.Entry<Object, Object>> traverseFeatureJoinFlat_BACKUP(RadigMatchTrie other, boolean equalOnly, boolean containOnly){
		MultiTreeMap<Object, Object> matchedFeatures = new MultiTreeMap<Object, Object>();
		this.traverseFeatureJoin(other, equalOnly, containOnly, matchedFeatures);
		return matchedFeatures.entrySet();
	}

	public static String[][] radigJoinJTSGeometry(RadigMatchTrie trieA, RadigMatchTrie trieB, boolean equalOnly, boolean containOnly){
		Set<Map.Entry<Object, Object>> matches = trieA.traverseFeatureJoinFlat(trieB, equalOnly, containOnly);
		TreeMap<String, String> idBuff = new TreeMap();
		int count = 0;
		for(Entry entry:matches){
			Geometry geomA = (Geometry) entry.getKey();
			Geometry geomB = (Geometry) entry.getValue();
			if(geomA.intersects(geomB)) {
				idBuff.put((String)geomA.getUserData(), (String)geomB.getUserData());
			}
		}
		String[][] rtn = new String[idBuff.size()][2];
		int idx = 0;
		for(Entry entry:idBuff.entrySet()){
			rtn[idx][0] = (String) entry.getKey();
			rtn[idx][1] = (String) entry.getValue();
			idx++;
		}
		return rtn;
	}

	public static String[][] radigJoinObjRef(RadigMatchTrie trieA, RadigMatchTrie trieB, boolean equalOnly, boolean containOnly){
		Set<Map.Entry<Object, Object>> matches = trieA.traverseFeatureJoinFlat(trieB, equalOnly, containOnly);
		TreeMap<String, String> idBuff = new TreeMap();
		int count = 0;
		for(Entry entry:matches){
			RadigObjRef refA = (RadigObjRef) entry.getKey();
			RadigObjRef refB = (RadigObjRef) entry.getValue();
			Geometry geomA = (Geometry) refA.obj;
			Geometry geomB = (Geometry) refB.obj;
			if (geomA.intersects(geomB)) {
				idBuff.put(refA.id, refB.id);
			}
		}
		String[][] rtn = new String[idBuff.size()][2];
		int idx = 0;
		for(Entry entry:idBuff.entrySet()){
			rtn[idx][0] = (String) entry.getKey();
			rtn[idx][1] = (String) entry.getValue();
			idx++;
		}
		return rtn;
	}

	/**
	 *  One way match: for reference in trieA, find matches in trieB (reference_B.StartsWith(reference_A)
	 * @param trieA
	 * @param trieB
	 * @param equalOnly
	 * @param containOnly
	 * @return
	 */
	public static String[][] radigOneWayJoinObjRef(RadigMatchTrie trieA, RadigMatchTrie trieB, boolean equalOnly, boolean containOnly){
		List<Map.Entry<Object, Object>> matches = trieA.traverseFeatureMatchFlat(trieB, equalOnly, containOnly);
		TreeMap<String, String> idBuff = new TreeMap();
		int count = 0;
		for(Entry entry:matches){
			RadigObjRef refA = (RadigObjRef) entry.getKey();
			RadigObjRef refB = (RadigObjRef) entry.getValue();
			Geometry geomA = (Geometry) refA.obj;
			Geometry geomB = (Geometry) refB.obj;
			if (geomA.intersects(geomB)) {
				idBuff.put(refA.id, refB.id);
			}
		}
		String[][] rtn = new String[idBuff.size()][2];
		int idx = 0;
		for(Entry entry:idBuff.entrySet()){
			rtn[idx][0] = (String) entry.getKey();
			rtn[idx][1] = (String) entry.getValue();
			idx++;
		}
		return rtn;
	}
	static void test0(){
//		String[][] refs1 = {{"001234", "1" }, {"0023", "2"}, {"002357", "3"}, {"00244567", "17"}, {"013456", "4"}, {"013456", "15"}, {"123456", "5"},  {"1234", "6"}};
//		String[][] refs2 = {{"00123456","7"}, {"0023","8"}, {"002378", "9"}, {"002445","10"},{"002445","16"},{"01345679","11"}, {"1234","12"}, {"0134","13"}, {"0134567962", "14"}};
//		RadigMatchTrie t1 = RadigMatchTrie.createRadigMatchTrie();
//		RadigMatchTrie t2 = RadigMatchTrie.createRadigMatchTrie();
//		for(String[] ref:refs1) {
//			t1.insertRadigRef(ref[0], ref[1]);
//		}
//		for(String[] ref:refs2) {
//			t2.insertRadigRef(ref[0], ref[1]);
//		}
//		MultiTreeMap<String, String> matchT12 = new MultiTreeMap<String, String> ();
//		MultiTreeMap<String, String> matchT21 = new MultiTreeMap<String, String> ();
//		//t1.traverseFeatureMatch(t2, false, false, matchT12);
//		t1.traverseFeatureJoin(t2, false, false, matchT12);
//		//t2.traverseFeatureMatch(t1, false, false, matchT21);
//		t1.resetLastVisitedChildFlag();
//		t2.resetLastVisitedChildFlag();
//		t2.traverseFeatureJoin(t1, false, false, matchT21);
//		System.out.println("***1 to 2***");
//		for(String key:matchT12.keySet()) {
//			Collection<String> values = matchT12.get(key);
//			System.out.println(key+ " -> ");
//			for(String str:values) {
//				System.out.println(str);
//			}
//			System.out.println("--------");
//		}
//		System.out.println("\n***2 to 1***");
//		for(String key:matchT21.keySet()) {
//			Collection<String> values = matchT21.get(key);
//			System.out.println(key+ " -> ");
//			for(String str:values) {
//				System.out.println(str);
//			}
//			System.out.println("--------");
//		}
//		System.out.println("\n***flatten***");
//		t1.resetLastVisitedChildFlag();
//		t2.resetLastVisitedChildFlag();
//		//
//		Set<Map.Entry<String, String>> pairs = t1.traverseFeatureJoinFlat(t2, false, false);
//		for(Map.Entry<String, String> entry:pairs) {
//			System.out.println(entry.getKey() + " -> "+entry.getValue());
//		}
	}

	static void testGem(){
		double pointRes = 1.0;
		double maxPlgExt = 20.0;
		int sz = 10000;
		//double bndMin = 1000.0;
		double bndMin = 50.0;
		double bndExt = sz*bndMin;
		GeometryFactory gf = new GeometryFactory();
		Geometry[] g1 = new Geometry[sz];
		Geometry[] g2 = new Geometry[sz];
		Random random = new Random(0);
		TreeMap<String, Geometry> tm1 = new TreeMap<String, Geometry>();
		TreeMap<String, Geometry> tm2 = new TreeMap<String, Geometry>();
		RadigMatchTrie t1 = RadigMatchTrie.createRadigMatchTrie();
		RadigMatchTrie t2 = RadigMatchTrie.createRadigMatchTrie();
		int refCnt1 = 0;
		int refCnt2 = 0;
		for(int i = 0; i < sz; ++i){
			//double x = random.nextDouble()*bndExt + bndMin;
			double x = (i+1)*bndMin;
			//double y = random.nextDouble()*bndExt + bndMin;
			double y = x;
			g1[i] = gf.createPoint(new Coordinate(x, y));
			//g2[i] = g1[i].buffer(random.nextDouble()*maxPlgExt, 1);
			g2[i] = g1[i].buffer(maxPlgExt, 1);
			g1[i].setUserData(Integer.toString(i));
			g2[i].setUserData(Integer.toString(i));
			String[] refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) g1[i], true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, g1[i]);
				refCnt1++;
			}
			String[] refs2 = Radig_BNG.compBNGRadigAdaptiveInt(g2[i], true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, g2[i]);
				refCnt2++;
			}
			Point ptY = gf.createPoint(new Coordinate(x, y+bndExt));
			ptY.setUserData(i+sz);
			refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) ptY, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, g1[i]);
				refCnt1++;
			}
			Geometry plgX = gf.createPoint(new Coordinate(x+bndExt, y)).buffer(maxPlgExt, 1);
			plgX.setUserData((i+sz));
			refs2 = Radig_BNG.compBNGRadigAdaptiveInt(plgX, true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, g2[i]);
				refCnt2++;
			}
		}
		System.out.println(refCnt1 + " - " + refCnt2);
		String[][] rlt = radigJoinJTSGeometry(t1, t2, false, false);
		int match = 0;
		int notmatch = 0;
		for(int i = 0; i < rlt.length; ++i){
			String[] pair = rlt[i];
			if(pair[0].compareTo(pair[1]) != 0){
				notmatch++;
			}else{
				match++;
			}
		}
		System.out.println("Total: "+rlt.length + " - match: "+match+ " - not match: "+notmatch);
	}

	//
	static void generateSampleData(int dim, String fnA, String fnB){
		GeometryFactory gf = new GeometryFactory();
		String delim = ";";
		int tgSegment = 1;
		double pointStep = 50.0;
		double buffSz = 20.0;
		double offset = pointStep*dim;
		try {
			PrintWriter writerA = new PrintWriter(fnA);
			writerA.println("id_a;geom_a");
			PrintWriter writerB = new PrintWriter(fnB);
			writerB.println("id_b;geom_b");
			int idCnt = 0;
			for(int i = 1; i <= dim;++i){
				double x = pointStep*i;
				double y = x;
				Point pt = gf.createPoint(new Coordinate(x, y));
				writerA.println(idCnt + delim+pt.toText());
				idCnt++;
				Point pt2 = gf.createPoint(new Coordinate(x+0.1, y+0.1));
				writerA.println(idCnt + delim+pt.toText());
				Geometry plg = pt.buffer(buffSz, tgSegment);
				writerB.println(idCnt+delim+plg.toText());
				Point ptY = gf.createPoint(new Coordinate(x, y+offset));
				writerA.println((idCnt+dim) + delim+ptY.toText());
				Geometry plgX = gf.createPoint(new Coordinate(x+offset, y)).buffer(buffSz, tgSegment);
				writerB.println((idCnt+dim)+delim+plgX.toText());
				idCnt++;
			}
			for(int i = 1; i <= dim;++i){
				double x = pointStep*i+offset;
				double y = x;
				Point pt = gf.createPoint(new Coordinate(x, y));
				writerA.println(idCnt + delim+pt.toText());
				Geometry plg = pt.buffer(buffSz, tgSegment);
				writerB.println(idCnt+delim+plg.toText());
				Point ptY = gf.createPoint(new Coordinate(x, y+offset));
				writerA.println((idCnt+dim) + delim+ptY.toText());
				Geometry plgX = gf.createPoint(new Coordinate(x+offset, y)).buffer(buffSz, tgSegment);
				writerB.println((idCnt+dim)+delim+plgX.toText());
				idCnt++;
			}
			writerA.close();
			writerB.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	static void testRadig(){
		WKTReader wkt = new WKTReader();
		try {
			RadigMatchTrie t1 = RadigMatchTrie.createRadigMatchTrie();
			RadigMatchTrie t2 = RadigMatchTrie.createRadigMatchTrie();

			Geometry pt1 = wkt.read("POINT (50.0 50.0)");
			pt1.setUserData("point_1");
			Geometry pt2 = wkt.read("POINT (90.0 90.0)");
			pt2.setUserData("point_2");
			Geometry pt3 = wkt.read("POINT (500001.0 200001.0)");
			pt3.setUserData("point_3");
			Geometry pt4 = wkt.read("POINT (200.0 200.0)");
			pt4.setUserData("point_4");
			Geometry pt5 = wkt.read("POINT (50.0 50.0)");
			pt5.setUserData("point_5");

			Geometry plg1 = wkt.read("POLYGON ((20.0 20.0, 80.0 20.0, 80.0 80.0, 20.0 80.0, 20.0 20.0))");
			plg1.setUserData("polygon_1");
			Geometry plg2 = wkt.read("POLYGON ((85.0 85.0, 95.0 85.0, 95.0 95.0, 85.0 95.0, 85.0 85.0))");
			plg2.setUserData("polygon_2");
			Geometry plg3 = wkt.read("POLYGON ((500000.0 200000.0, 500002.0 200000.0, 500002.0 200002.0, 500000.0 200002.0, 500000.0 200000.0))");
			plg3.setUserData("polygon_3");
			Geometry plg4 = wkt.read("POLYGON ((120.0 120.0, 130.0 120.0, 130.0 130.0, 120.0 130.0, 120.0 120.0))");
			plg4.setUserData("polygon_4");

			String[] refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) pt1, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, pt1);
				System.out.println(ref);
			}
			refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) pt2, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, pt2);
				System.out.println(ref);
			}
			refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) pt3, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, pt3);
				System.out.println(ref);
			}
			refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) pt4, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, pt4);
				System.out.println(ref);
			}
			refs1 = Radig_BNG.compPointBNGRadigRefRes((Point) pt5, true, 1.0);
			for(String ref:refs1){
				t1.insertRadigRef(ref, pt5);
				System.out.println(ref);
			}

			System.out.println("--------------------------");
			String[] refs2 = Radig_BNG.compBNGRadigAdaptiveInt(plg1, true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, plg1);
				System.out.println(ref);
			}
			refs2 = Radig_BNG.compBNGRadigAdaptiveInt(plg2, true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, plg2);
				System.out.println(ref);
			}
			refs2 = Radig_BNG.compBNGRadigAdaptiveInt(plg3, true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, plg3);
				System.out.println(ref);
			}
			refs2 = Radig_BNG.compBNGRadigAdaptiveInt(plg3, true, 1, 1, 1.0, 0.5, 0.5);
			for(String ref:refs2){
				t2.insertRadigRef(ref, plg4);
				System.out.println(ref);
			}
			System.out.println("--------------------------");

			Set<Map.Entry<Object, Object>> rlt = t1.traverseFeatureJoinFlat(t2, false, false);
			for(Entry entry:rlt){
				System.out.println(((Geometry)entry.getKey()).getUserData() + " - " + ((Geometry)entry.getValue()).getUserData());
			}
			System.out.println("--------------------------");
			t1.resetLastVisitedChildFlag();
			t2.resetLastVisitedChildFlag();
			String[][] predMatch = radigJoinJTSGeometry(t1, t2, false, false);
			for(String[] pair:predMatch){
				System.out.println(pair[0] + " --- "+ pair[1]);
			}
			System.out.println("--------------------------");
			List<String> matchedRefs = new LinkedList<>();
			t2.queryRadigRef("DSV00000000000000", false, matchedRefs);
			for(String ref:matchedRefs){
				System.out.println(ref);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	static void testRadig2(){
		RadigMatchTrie t1 = RadigMatchTrie.createRadigMatchTrie();
		RadigMatchTrie t2 = RadigMatchTrie.createRadigMatchTrie();
		WKTReader reader = new WKTReader();
		String[] refA = {"DSV0000", "DSV00001010", "DTQ0000", "DTQ00001010", "DTU0000"};
		String[] featA = {
				"POINT (431890 1156178)",
				"POINT (431890 1156179)",
				"POINT (431890 1156180)",
				"POINT (431845 1156161)",
				"POINT (431845 1156162)"
		};
		//
		String[] refB = {"DSV0000","DSV00001111", "DTQ00001010", "DTU0000"};
		String[] featB = {
				"POINT (431890 1156178)",
				"POINT (431845 1156160)",
				"POINT (431845 1156162)",
				"POINT (431845 1156163)"
		};

		for(int i = 0; i < refA.length; ++i){
			Geometry geom = null;
			try {
				geom = reader.read(featA[i]); //
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			geom.setUserData(refA[i]);
			t1.insertRadigRef(refA[i], geom); // two features with the same geometry will have issue here
		}
		for(int i = 0; i < refB.length; ++i){
			Geometry geom = null;
			try {
				geom = reader.read(featB[i]);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
			geom.setUserData(refB[i]);
			t2.insertRadigRef(refB[i], geom);
		}
 		List<Map.Entry<Object, Object>> rlt1 = t1.traverseFeatureMatchFlat(t2, false, false);
		for(Entry entry:rlt1){
			System.out.println(entry.getKey() + " --- "+entry.getValue());
		}
		t1.resetLastVisitedChildFlag();
		t2.resetLastVisitedChildFlag();
		List<Map.Entry<Object, Object>> rlt2 = t2.traverseFeatureMatchFlat(t1, false, false);
		for(Entry entry:rlt2){
			System.out.println(entry.getKey() + " --- "+entry.getValue());
		}
		System.out.println("__________________________");
		Set<Map.Entry<Object, Object>> rltSet = t1.traverseFeatureJoinFlat(t2, false, false);
		for(Entry<Object, Object> entry:rltSet){
			System.out.println((String)entry.getKey() + " --- "+(String)(((Geometry)entry.getValue()).toText()));
		}
		System.out.println("__________________________");
		List<String> refs = new LinkedList<>();
		t1.getAllRefs(null, refs);
	}

	static void testRadig3(){
		RadigMatchTrie t1 = RadigMatchTrie.createRadigMatchTrie();
		RadigMatchTrie t2 = RadigMatchTrie.createRadigMatchTrie();
		String[] refA = {"DHU01300111103111420103",
				"DHU01300111103111321143",
				"DHU01300111103111420102",
				"DHU01300111103111321142",
				"DHU01300111103101411001",
				"DHU01300111103101410041",
				"DHU01300111103101411000",
				"DHU01300111103101410040",
				"DHU01300111010410430130"

		};
		String[] idA = {"30100000337","30100000337", "30100000337", "30100000337", "30100006803","30100006803","30100006803","30100006803","10025029923"};
		String[] geomA = {"POINT (431890 1156178)",
				"POINT (431890 1156178)",
				"POINT (431890 1156178)",
				"POINT (431890 1156178)",
				"POINT (431845 1156161)",
				"POINT (431845 1156161)",
				"POINT (431845 1156161)",
				"POINT (431845 1156161)",
				"POINT (431093.4 1156935.6)"
		};
		String[] idB = {
				"1", "2","3","4","5","6","7","8","9"
		};
		String[] refB = {"DHU01300111103100",
				"DHU01300111103110",
				"DHU01300111103101",
				"DHU01300111103111",
				"DHU01300111103111",
				"DHU0130011110321011",
				"DHU0130011110321012",
				"DHU0130011110321021",
				"DHU01300111104200"
		};
		String[] geomB = {
				"POLYGON ((431853.56 1156109.51, 431844.99 1156117.45, 431840.42 1156112.51, 431848.99 1156104.57, 431853.56 1156109.51))",
				"POLYGON ((431853.56 1156109.51, 431844.99 1156117.45, 431840.42 1156112.51, 431848.99 1156104.57, 431853.56 1156109.51))",
				"POLYGON ((431842.66 1156172.73, 431836 1156166.79, 431848.92 1156152.29, 431855.58 1156158.22, 431842.66 1156172.73))",
				"POLYGON ((431842.66 1156172.73, 431836 1156166.79, 431848.92 1156152.29, 431855.58 1156158.22, 431842.66 1156172.73))",
				"POLYGON ((431881.49 1156183, 431891.5 1156171.51, 431895.51 1156175, 431885.5 1156186.49, 431881.49 1156183))",
				"POLYGON ((431872.14 1156215.94, 431867.53 1156221.79, 431864.41 1156219.33, 431869.02 1156213.48, 431872.14 1156215.94))",
				"POLYGON ((431872.14 1156215.94, 431867.53 1156221.79, 431864.41 1156219.33, 431869.02 1156213.48, 431872.14 1156215.94))",
				"POLYGON ((431872.14 1156215.94, 431867.53 1156221.79, 431864.41 1156219.33, 431869.02 1156213.48, 431872.14 1156215.94))",
				"POLYGON ((431947.96 1156237.24, 431938.46 1156249.82, 431934.47 1156246.81, 431943.97 1156234.22, 431947.96 1156237.24))"
		};
		for(int i = 0; i < idA.length;++i){
			t1.insertRadigRef(refA[i], new RadigObjRef(idA[i], geomA[i]));
		}
		for(int i = 0; i < idB.length;++i){
			t2.insertRadigRef(refB[i], new RadigObjRef(idB[i], geomB[i]));
		}
		String[][] rlt = radigJoinObjRef(t1, t2, false, false);
		for(String[] pair:rlt){
			System.out.println(pair[0] + " --- "+pair[1]);
		}
		System.out.println("done!");
	}

	public static void main(String[] args) {
		//testRadig();
		testRadig2();
//		testRadig3();
		//testGem();
		//generateSampleData(10000, "c:/MapData/Radig/data_a2.csv", "c:/MapData/Radig/data_b2.csv");
	}
}