/**
 * customised version to handle BNG-style reference (7 total integer digits each ordinate, single or 2x5 dual divisor)
 * reference prefix is changed to SXX or DXX where S/D for single or dual divisor and XX for the leading 100km grid
 * reference from the original BNG scheme.
 *
 * Note that references for coordiantes in sea (within the BNG range) are also supported.
 * <p>
 * <p>
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * <p>
 * version 1.0
 * <p>
 * Date: 2025-01-15
 * <p>
 * Copyright (C) 2025 Ordnance Survey
 * <p>
 * Licensed under the Open Government Licence v3.0 (the "License");
 * <p>
 * you may not use this file except in compliance with the License.
 * <p>
 * You may obtain a copy of the License at
 * <p>
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Author: Sheng Zhou (Sheng.Zhou@os.uk)
 * <p>
 * version 0.1
 * <p>
 * Date: 2022-09-28
 * <p>
 * Copyright (C) 2022 Ordnance Survey
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class Radig_BNG {
    // 10 base single divisor
    private static final Map<String, String> BNG_PREFIX_MAP10;
    // 10 base dual divisor 2x5
    private static final Map<String, String> BNG_PREFIX_MAP25;
    private static final Map<String, String> BNG_PREFIX_REVERSE_MAP10;
    private static final Map<String, String> BNG_PREFIX_REVERSE_MAP25;
    static {
        Map<String, String> prefixMap10 = new TreeMap<String, String>();
        Map<String, String> prefixMap25 = new TreeMap<String, String>();
        Map<String, String> prefixRevMap10 = new TreeMap<String, String>();
        Map<String, String> prefixRevMap25 = new TreeMap<String, String>();
        //
        prefixMap10.put("0000", "SV");        
        prefixMap10.put("0010", "SW");
        prefixMap10.put("0020", "SX");
        prefixMap10.put("0030", "SY");
        prefixMap10.put("0040", "SZ");
        prefixMap10.put("0050", "TV");
        prefixMap10.put("0060", "TW"); // sea

        prefixMap10.put("0001", "SQ"); // sea
        prefixMap10.put("0011", "SR");
        prefixMap10.put("0021", "SS");
        prefixMap10.put("0031", "ST");
        prefixMap10.put("0041", "SU");
        prefixMap10.put("0051", "TQ");
        prefixMap10.put("0061", "TR");

        prefixMap10.put("0002", "SL"); // sea
        prefixMap10.put("0012", "SM");
        prefixMap10.put("0022", "SN");
        prefixMap10.put("0032", "SO");
        prefixMap10.put("0042", "SP");
        prefixMap10.put("0052", "TL");
        prefixMap10.put("0062", "TM");

        prefixMap10.put("0003", "SF"); // sea
        prefixMap10.put("0013", "SG"); // sea
        prefixMap10.put("0023", "SH");
        prefixMap10.put("0033", "SJ");
        prefixMap10.put("0043", "SK");
        prefixMap10.put("0053", "TF");
        prefixMap10.put("0063", "TG");

        prefixMap10.put("0004", "SA"); // sea
        prefixMap10.put("0014", "SB"); // sea
        prefixMap10.put("0024", "SC");
        prefixMap10.put("0034", "SD");
        prefixMap10.put("0044", "SE");
        prefixMap10.put("0054", "TA");
        prefixMap10.put("0064", "TB"); // sea

        prefixMap10.put("0005", "NV"); // sea
        prefixMap10.put("0015", "NW");
        prefixMap10.put("0025", "NX");
        prefixMap10.put("0035", "NY");
        prefixMap10.put("0045", "NZ");
        prefixMap10.put("0055", "OV"); // sea
        prefixMap10.put("0065", "OW"); // sea

        prefixMap10.put("0006", "NQ"); // sea
        prefixMap10.put("0016", "NR");
        prefixMap10.put("0026", "NS");
        prefixMap10.put("0036", "NT");
        prefixMap10.put("0046", "NU");
        prefixMap10.put("0056", "OQ"); // sea
        prefixMap10.put("0066", "OR"); // sea

        prefixMap10.put("0007", "NL");
        prefixMap10.put("0017", "NM");
        prefixMap10.put("0027", "NN");
        prefixMap10.put("0037", "NO");
        prefixMap10.put("0047", "NP"); // sea
        prefixMap10.put("0057", "OL"); // sea
        prefixMap10.put("0067", "OM"); // sea
        
        prefixMap10.put("0008", "NF");
        prefixMap10.put("0018", "NG");
        prefixMap10.put("0028", "NH");
        prefixMap10.put("0038", "NJ");
        prefixMap10.put("0048", "NK");
        prefixMap10.put("0058", "OF"); // sea
        prefixMap10.put("0068", "OG"); // sea
        
        prefixMap10.put("0009", "NA");
        prefixMap10.put("0019", "NB");
        prefixMap10.put("0029", "NC");
        prefixMap10.put("0039", "ND");
        prefixMap10.put("0049", "NE"); // sea
        prefixMap10.put("0059", "OA"); // sea
        prefixMap10.put("0069", "OB"); // sea

        prefixMap10.put("0100", "HV"); // sea
        prefixMap10.put("0110", "HW");
        prefixMap10.put("0120", "HX");
        prefixMap10.put("0130", "HY");
        prefixMap10.put("0140", "HZ");
        prefixMap10.put("0150", "JV");
        prefixMap10.put("0160", "JW");

        prefixMap10.put("0101", "HQ"); // sea
        prefixMap10.put("0111", "HR"); // sea
        prefixMap10.put("0121", "HS"); // sea
        prefixMap10.put("0131", "HT");
        prefixMap10.put("0141", "HU");
        prefixMap10.put("0151", "JQ"); // sea
        prefixMap10.put("0161", "JR"); // sea

        prefixMap10.put("0102", "HL"); // sea
        prefixMap10.put("0112", "HM"); // sea
        prefixMap10.put("0122", "HN"); // sea
        prefixMap10.put("0132", "HO"); // sea
        prefixMap10.put("0142", "HP");
        prefixMap10.put("0152", "JL"); // sea
        prefixMap10.put("0162", "JM"); // sea
        
        BNG_PREFIX_MAP10 = Collections.unmodifiableMap(prefixMap10);
        //
        for(Map.Entry<String, String> entry:prefixMap10.entrySet()) {
            prefixRevMap10.put(entry.getValue(), entry.getKey());
        }
        BNG_PREFIX_REVERSE_MAP10 = Collections.unmodifiableMap(prefixRevMap10);

        prefixMap25.put("00000000", "SV");        
        prefixMap25.put("00000010", "SW");
        prefixMap25.put("00000020", "SX");
        prefixMap25.put("00000030", "SY");
        prefixMap25.put("00000040", "SZ");
        prefixMap25.put("00001000", "TV");
        prefixMap25.put("00001010", "TW"); // sea

        prefixMap25.put("00000001", "SQ"); // sea
        prefixMap25.put("00000011", "SR");
        prefixMap25.put("00000021", "SS");
        prefixMap25.put("00000031", "ST");
        prefixMap25.put("00000041", "SU");
        prefixMap25.put("00001001", "TQ");
        prefixMap25.put("00001011", "TR");

        prefixMap25.put("00000002", "SL"); // sea
        prefixMap25.put("00000012", "SM");
        prefixMap25.put("00000022", "SN");
        prefixMap25.put("00000032", "SO");
        prefixMap25.put("00000042", "SP");
        prefixMap25.put("00001002", "TL");
        prefixMap25.put("00001012", "TM");

        prefixMap25.put("00000003", "SF"); // sea
        prefixMap25.put("00000013", "SG"); // sea
        prefixMap25.put("00000023", "SH");
        prefixMap25.put("00000033", "SJ");
        prefixMap25.put("00000043", "SK");
        prefixMap25.put("00001003", "TF");
        prefixMap25.put("00001013", "TG");

        prefixMap25.put("00000004", "SA"); // sea
        prefixMap25.put("00000014", "SB"); // sea
        prefixMap25.put("00000024", "SC");
        prefixMap25.put("00000034", "SD");
        prefixMap25.put("00000044", "SE");
        prefixMap25.put("00001004", "TA");
        prefixMap25.put("00001014", "TB"); // sea

        prefixMap25.put("00000100", "NV"); // sea
        prefixMap25.put("00000110", "NW");
        prefixMap25.put("00000120", "NX");
        prefixMap25.put("00000130", "NY");
        prefixMap25.put("00000140", "NZ");
        prefixMap25.put("00001100", "OV"); // sea
        prefixMap25.put("00001110", "OW"); // sea

        prefixMap25.put("00000101", "NQ"); // sea
        prefixMap25.put("00000111", "NR");
        prefixMap25.put("00000121", "NS");
        prefixMap25.put("00000131", "NT");
        prefixMap25.put("00000141", "NU");
        prefixMap25.put("00001101", "OQ"); // sea
        prefixMap25.put("00001111", "OR"); // sea

        prefixMap25.put("00000102", "NL");
        prefixMap25.put("00000112", "NM");
        prefixMap25.put("00000122", "NN");
        prefixMap25.put("00000132", "NO");
        prefixMap25.put("00000142", "NP"); // sea
        prefixMap25.put("00001102", "OL"); // sea
        prefixMap25.put("00001112", "OM"); // sea
        
        prefixMap25.put("00000103", "NF");
        prefixMap25.put("00000113", "NG");
        prefixMap25.put("00000123", "NH");
        prefixMap25.put("00000133", "NJ");
        prefixMap25.put("00000143", "NK");
        prefixMap25.put("00001103", "OF"); // sea
        prefixMap25.put("00001113", "OG"); // sea
        
        prefixMap25.put("00000104", "NA");
        prefixMap25.put("00000114", "NB");
        prefixMap25.put("00000124", "NC");
        prefixMap25.put("00000134", "ND");
        prefixMap25.put("00000144", "NE"); // sea
        prefixMap25.put("00001104", "OA"); // sea
        prefixMap25.put("00001114", "OB"); // sea

        prefixMap25.put("00010000", "HV"); // sea
        prefixMap25.put("00010010", "HW");
        prefixMap25.put("00010020", "HX");
        prefixMap25.put("00010030", "HY");
        prefixMap25.put("00010040", "HZ");
        prefixMap25.put("00011001", "JV"); // sea
        prefixMap25.put("00011011", "JW"); // sea

        prefixMap25.put("00010001", "HQ"); // sea
        prefixMap25.put("00010011", "HR"); // sea
        prefixMap25.put("00010021", "HS"); // sea
        prefixMap25.put("00010031", "HT");
        prefixMap25.put("00010041", "HU");
        prefixMap25.put("00011001", "JQ"); // sea
        prefixMap25.put("00011011", "JR"); // sea

        prefixMap25.put("00010002", "HP"); // sea
        prefixMap25.put("00010012", "HP"); // sea
        prefixMap25.put("00010022", "HP"); // sea
        prefixMap25.put("00010032", "HP"); // sea
        prefixMap25.put("00010042", "HP");
        prefixMap25.put("00011002", "HP"); // sea
        prefixMap25.put("00011012", "HP"); // sea

        BNG_PREFIX_MAP25 = Collections.unmodifiableMap(prefixMap25);
        //
        for(Map.Entry<String, String> entry:prefixMap25.entrySet()) {
            prefixRevMap25.put(entry.getValue(), entry.getKey());
        }
        BNG_PREFIX_REVERSE_MAP25 = Collections.unmodifiableMap(prefixRevMap25);

    }
    private static final int TOTAL_INT_DIGIT_BNG = 7;
    private static final int TOTAL_DEC_DIGIT_BNG = 3; 
    private static final String RADIG_PREFIX_BNG = "9725"; // base-1 (range 1-9-A-F for base 2 to 16); 7 integer digits, first divisor 2, second divisor 5
    //
    boolean dualDivisor;
    RadigGrid grid;

    /**
     *
     * @param hasDualDivisor
     */
    public Radig_BNG(boolean hasDualDivisor) {
        dualDivisor = hasDualDivisor;
        grid = new RadigGrid(10, TOTAL_INT_DIGIT_BNG, TOTAL_INT_DIGIT_BNG + TOTAL_DEC_DIGIT_BNG, dualDivisor?2:10, true);
    }
    //

    /** Compute BNG-styled Radig references for a geometry on a fixed resolution based on the extent of the input geometry
     * @param geom the input geometry
     * @param extentDivisor the initial extent from the dimension of the input geometry will be divided by divisor to a smaller extent value to decide resolution
     * @param useLowerBound if true, use the resolution <= extent; otherwise, use the resolution > extent from the resolution bound array
     * @param intPolicy 0: Any intersection; i: interiors of input geometry and cell geometry intersect; 2: input geometry contains cell geometry (not used now)
     * @param intRlts if the cell geometry intersects with input geometry and cntRlts is null, the cell ref will be added to the list for returning result
     * @param cntRlts if not null and the cell is fully contained by the input geometry, the reference is added to this list instead
     * @return number of references generated
     */
    public int compBNGRadigRefExt(Geometry geom, double extentDivisor, boolean useLowerBound, int intPolicy, List<String> intRlts, List<String> cntRlts) {
        List<String> intRtns = new LinkedList<String>();
        List<String> cntRtns = cntRlts!=null?new LinkedList<String>():null;
        grid.compRadigRefExt(geom, extentDivisor, useLowerBound, intPolicy, intRtns, cntRtns);
        convertRadigRefToBNGRadig(intRtns, intRlts, dualDivisor);
        int cnt = intRtns.size();
        if(cntRlts!=null) {
            cnt+=cntRtns.size();
            convertRadigRefToBNGRadig(cntRtns, cntRlts, dualDivisor);
        }
        return cnt;
    }

    /**
     * Compute BNG-styled Radig references for a geometry with subdivision. The initial resolution is the UB from the extent of the input geometry.
     *
     * @param geom
     * @param intPolicy 0: any intersection; 1: interiors intersect
     * @param maxDivisonLevel maximum number of subdivisions to be performed. 0 for no subdivision and negative for infinite (restricted by number of resolutions in RADIG definition)
     * @param eaThreshold
     * @param elThreshold
     * @param intRlts
     * @param cntRlts
     * @return number of references generated
     */
    public int compBNGRadigRefAdaptive(Geometry geom, int intPolicy, int maxDivisonLevel, double eaThreshold, double elThreshold, List<String> intRlts, List<String> cntRlts) {
        List<String> intRtns = new LinkedList<String>();
        List<String> cntRtns = cntRlts!=null?new LinkedList<String>():null;
        grid.compRadigRefAdaptive(geom, intPolicy, maxDivisonLevel, eaThreshold, elThreshold, intRtns, cntRtns);
        convertRadigRefToBNGRadig(intRtns, intRlts, dualDivisor);
        int cnt = intRtns.size();
        if(cntRlts!=null) {
            cnt+=cntRtns.size();
            convertRadigRefToBNGRadig(cntRtns, cntRlts, dualDivisor);
        }
        return cnt;
    }

    /**
     * Compute BNG-styled Radig references for a geometry in fixed resolution derived from a provisional input resolution
     * @param geom
     * @param resolution pr0visional resultion
     * @param useLowerBound if true, LB of the resolution will be used; otherwise, UB of resolution will be used
     * @param intPolicy
     * @param intRlts
     * @param cntRlts
     * @return
     */
    public int compBNGRadigRefRes(Geometry geom, double resolution, boolean useLowerBound, int intPolicy, List<String> intRlts, List<String> cntRlts) {
        List<String> intRtns = new LinkedList<String>();
        List<String> cntRtns = cntRlts!=null?new LinkedList<String>():null;
        grid.compRadigRefRes(geom, resolution, useLowerBound, intPolicy, intRtns, cntRtns);
        convertRadigRefToBNGRadig(intRtns, intRlts, dualDivisor);
        int cnt = intRtns.size();
        if(cntRlts!=null) {
            cnt+=cntRtns.size();
            convertRadigRefToBNGRadig(cntRtns, cntRlts, dualDivisor);
        }
        return cnt;
    }
    /**
     *
     * @param point
     * @param resolution
     * @param rlts
     * @return
     */
    public int compPointBNGRadigRef(Point point, double resolution, List<String> rlts) {
        List<String> rtn = new LinkedList<String>();
        grid.compPointRadigRef(point.getX(), point.getY(), resolution, rtn);
        convertRadigRefToBNGRadig(rtn, rlts, dualDivisor);
        return rtn.size();
    }
    /**
     *
     * @param x
     * @param y
     * @param numDigits
     * @param endAtFirstDivisor
     * @param rlts
     * @return
     */
    public int compPointBNGRadigRef(double x, double y, int numDigits, boolean endAtFirstDivisor, List<String> rlts) {
        List<String> rtn = new LinkedList<String>();
        grid.compPointRadigRef(x, y, numDigits, endAtFirstDivisor, rtn);
        convertRadigRefToBNGRadig(rtn, rlts, dualDivisor);
        return rtn.size();
    }
    //
    /**
     * @param iLRefs
     * @param bngRadigRefs
     */
//    private void convertILRefToBNGRadig(List<String> iLRefs, List<String> bngRadigRefs) {
//    	for(String ref:iLRefs) {
//    		if(dualDivisor) {
//    	    	String iLPrefix = ref.substring(0, 8);
//    	    	String bngPrefix = BNG_PREFIX_MAP25.get(iLPrefix);
//    	    	bngRadigRefs.add(bngPrefix + ref.substring(8));
//    		}else {
//    	    	String iLPrefix = ref.substring(0, 4);
//    	    	String bngPrefix = BNG_PREFIX_MAP10.get(iLPrefix);
//    	    	bngRadigRefs.add(bngPrefix + ref.substring(4));
//    		}
//    	}
//    }
/*************************************
 * 
 *  static utility methods 
 *
 ************************************/
    public static String converRadigRefToBNGRadig(String radigRef, boolean hasDualDivisor) {
        String ilRef = RadigGrid.radigRefToILRef(radigRef);
        int prefixLen = hasDualDivisor?8:4;
        String iLPrefix = ilRef.substring(0, prefixLen);
        String bngPrefix = hasDualDivisor?"D"+BNG_PREFIX_MAP25.get(iLPrefix):"S"+BNG_PREFIX_MAP10.get(iLPrefix);
        return bngPrefix + ilRef.substring(prefixLen);
    }
    
    public static void convertRadigRefToBNGRadig(List<String> iLRefs, List<String> bngRadigRefs, boolean hasDualDivisor) {
        for(String ref:iLRefs) {
            bngRadigRefs.add(converRadigRefToBNGRadig(ref, hasDualDivisor));
        }
    }
    public static String convertBNGRadigToILRef(String bngRadig) {
        char bngRadigDivisorFlag = bngRadig.charAt(0);
        boolean hasDualDivisor = bngRadigDivisorFlag == 'D'?true:false;
        String bngPrefix = bngRadig.substring(1, 3);
        String iLRefHeader = hasDualDivisor?BNG_PREFIX_REVERSE_MAP25.get(bngPrefix):BNG_PREFIX_REVERSE_MAP10.get(bngPrefix);
        return iLRefHeader+bngRadig.substring(3);
    }
    public static void convertBNGRadigToILRef(List<String> bngRadigRefs, List<String> iLRefs, boolean hasDualDivisor) {
        for(String ref:bngRadigRefs) {
            iLRefs.add(convertBNGRadigToILRef(ref));
        }
    }
    /** calculate the resolution a BNGRadig ref 
     * @param bngRadigRef
     * @param firstDivisor 10 or 2. if 10, the code uses single divisor
     * @return
     */
    public static double calcResBNGRadig(String bngRadigRef) {
        char flag = bngRadigRef.charAt(0);
        int firstDivisor = flag =='D'?2:10;
        String subRef = bngRadigRef.substring(3); // dual divisor flag + BNG prefix
        double res = Radig_DigitInterleaver.calcResolutionILRef(subRef, 10, TOTAL_INT_DIGIT_BNG, firstDivisor);
        return res/100.0;
    }
    //
 /*************************************************
  * 
  *    static methods for external use
  *    
  *    
  ************************************************/
    /** Compute RadigBNG reference for geometry, using fixed resolution derived from the extent of the input geometry. No subdivision. Not testing if
     *  the cell geometry contains the input geometry
     *
     * @param geom
     * @param extentDivisor
     * @param useLowerBound
     * @param hasDualDivisor
     * @param intersectPolicy 0 for any intersection and 1 for intersect_interior
     * @return
     */
    public static String[] compBNGRadigExtInt(Geometry geom, boolean hasDualDivisor, int extentDivisor, boolean useLowerBound, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        List<String> intRlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefExt(geom, extentDivisor,useLowerBound,  intersectPolicy, intRlts, null);
        String[] refArray = new String[intRlts.size()];
        intRlts.toArray(refArray);
        return refArray;
    }
    /** Compute RadigBNG reference for geometry, using fixed resolution derived from extent of the geometry and an extent divisor, no sub-division. Testing
     * if the cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param extentDivisor
     * @param useLowerBound
     * @param hasDualDivisor
     * @param intersectPolicy
     * @return
     */
    public static String[][] compBNGRadigExtCnt(Geometry geom, boolean hasDualDivisor, int extentDivisor, boolean useLowerBound, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        List<String> intRlts = new LinkedList<String>();
        List<String> cntRlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefExt(geom, extentDivisor,useLowerBound,  intersectPolicy, intRlts, cntRlts);
        String[] intRefArray = new String[intRlts.size()];
        intRlts.toArray(intRefArray);
        String[] cntRefArray = new String[cntRlts.size()];
        cntRlts.toArray(cntRefArray);
        String[][] rtn = new String[2][];
        rtn[0] = intRefArray;
        rtn[1] = cntRefArray;
        return rtn;
    }
    
    /** Compute RadigBNG reference for geometry, using adaptive resolution and subdivison. Not testing if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param intersectPolicy
     * @param maxDivisonLevel
     * @param minDivRes subdivision will stop if division resolution is below minDivRes
     * @param eaThreshold
     * @param elThreshold
     * @return
     */
    public static String[] compBNGRadigAdaptiveInt(Geometry geom, boolean hasDualDivisor, int intersectPolicy, int maxDivisonLevel, double minDivRes, double eaThreshold, double elThreshold) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        bngGrid.grid.radigDef.setMinSubDivisionRes(minDivRes);
        List<String> rlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefAdaptive(geom, intersectPolicy, maxDivisonLevel, eaThreshold, elThreshold, rlts, null);
        String[] refArray = new String[rlts.size()];
        rlts.toArray(refArray);
        return refArray;
    }
    /**
     * Compute RadigBNG reference for geometry, using adaptive resolution and subdivison. Testing if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param intersectPolicy
     * @param maxDivisonLevel
     * @param minDivRes
     * @param eaThreshold
     * @param elThreshold
     * @return rtn[0] the intersecting refs; rtn[1] the contained refs
     */
    public static String[][] compBNGRadigAdaptiveCnt(Geometry geom, boolean hasDualDivisor, int intersectPolicy, int maxDivisonLevel, double minDivRes, double eaThreshold, double elThreshold) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        bngGrid.grid.radigDef.setMinSubDivisionRes(minDivRes);
        List<String> intRlts = new LinkedList<String>();
        List<String> cntRlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefAdaptive(geom, intersectPolicy, maxDivisonLevel, eaThreshold, elThreshold, intRlts, cntRlts);
        String[] intRefArray = new String[intRlts.size()];
        intRlts.toArray(intRefArray);
        String[] cntRefArray = new String[cntRlts.size()];
        cntRlts.toArray(cntRefArray);
        String[][] rtn = new String[2][];
        rtn[0] = intRefArray;
        rtn[1] = cntRefArray;
        return rtn;
    }
    
    /** Compute RadigBNG reference for geometry, using fixed resolution derived from a provisional resolution. No subdivison. Not testing
     * if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param resolution
     * @param useLowerBound
     * @param intersectPolicy
     * @return
     */
    public static String[] compBNGRadigResInt(Geometry geom, boolean hasDualDivisor, double resolution, boolean useLowerBound, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        List<String> rlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefRes(geom, resolution, useLowerBound, intersectPolicy, rlts, null);
        String[] refArray = new String[rlts.size()];
        rlts.toArray(refArray);
        return refArray;
    }
    /**
     * Compute RadigBNG reference for geometry, using fixed resolution derived from a provisional resolution. No subdivison.
     * Testing if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param resolution
     * @param useLowerBound
     * @param intersectPolicy
     * @return
     */
    public static String[][] compBNGRadigResCnt(Geometry geom, boolean hasDualDivisor, double resolution, boolean useLowerBound, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        List<String> intRlts = new LinkedList<String>();
        List<String> cntRlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefRes(geom, resolution, useLowerBound, intersectPolicy, intRlts, cntRlts);
        String[] intRefArray = new String[intRlts.size()];
        intRlts.toArray(intRefArray);
        String[] cntRefArray = new String[cntRlts.size()];
        cntRlts.toArray(cntRefArray);
        String[][] rtn = new String[2][];
        rtn[0] = intRefArray;
        rtn[1] = cntRefArray;
        return rtn;
    }
    /** Compute RadigBNG reference for geometry, using fixed resolution derived from number of digits to be encoded. No subdivison.
     * Not testing if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param numDigits
     * @param useLowerBound
     * @param endAtFirstDivisor
     * @param intersectPolicy
     * @return
     */
    public static String[] compBNGRadigDigitsInt(Geometry geom, boolean hasDualDivisor, int numDigits, boolean useLowerBound, boolean endAtFirstDivisor, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        double res = bngGrid.grid.radigDef.getResolutionNumDigits(numDigits, endAtFirstDivisor, null);
        List<String> rlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefRes(geom, res, useLowerBound, intersectPolicy, rlts, null);
        String[] refArray = new String[rlts.size()];
        rlts.toArray(refArray);
        return refArray;
    }
    /**
     * Compute RadigBNG reference for geometry, using fixed resolution derived from number of digits to be encoded. No subdivison.
     * Testing if cell geometry contains the input geometry
     * @param geom
     * @param hasDualDivisor
     * @param numDigits
     * @param useLowerBound
     * @param endAtFirstDivisor
     * @param intersectPolicy
     * @return
     */
    public static String[][] compBNGRadigDigitsCnt(Geometry geom, boolean hasDualDivisor, int numDigits, boolean useLowerBound, boolean endAtFirstDivisor, int intersectPolicy) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        double res = bngGrid.grid.radigDef.getResolutionNumDigits(numDigits, endAtFirstDivisor, null);
        List<String> intRlts = new LinkedList<String>();
        List<String> cntRlts = new LinkedList<String>();
        bngGrid.compBNGRadigRefRes(geom, res, useLowerBound, intersectPolicy, intRlts, cntRlts);
        String[] intRefArray = new String[intRlts.size()];
        intRlts.toArray(intRefArray);
        String[] cntRefArray = new String[cntRlts.size()];
        cntRlts.toArray(cntRefArray);
        String[][] rtn = new String[2][];
        rtn[0] = intRefArray;
        rtn[1] = cntRefArray;
        return rtn;
    }
    
    public static String[] compPointBNGRadigRefRes(Point point, boolean hasDualDivisor, double resolution){
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        double res = bngGrid.grid.radigDef.getResLB(resolution, null);
        List<String> rlts = new LinkedList<String>();
        bngGrid.compPointBNGRadigRef(point, resolution, rlts);
        String[] refArray = new String[rlts.size()];
        rlts.toArray(refArray);
        return refArray;
    }
    public String compPointBNGRadigRefSingle(Point point, double resolution) {
        List<String> rtn = new LinkedList<String>();
        grid.compPointRadigRef(point.getX(), point.getY(), resolution, rtn);
        return converRadigRefToBNGRadig(rtn.get(0), dualDivisor);
    }
    public static String compPointBNGRadigRefSingle(Point point, boolean hasDualDivisor, double resolution) {
        Radig_BNG bngGrid = new Radig_BNG(hasDualDivisor);
        double res = bngGrid.grid.radigDef.getResLB(resolution, null);
        return bngGrid.compPointBNGRadigRefSingle(point, resolution);
    }
    static void test0(){
        //String geomStr = "POLYGON((427000 234000, 428000 234000, 428000 235000, 427000 235000, 427000 234000))";
        //String geomStr =  "POLYGON((180000 880000, 250000 880000, 250000 910000, 180000 910000, 180000 880000))";
        //String geomStr = "POLYGON ((458709.44 242642.85, 458707.4 242656.99, 458706.3 242663.49, 458705.3 242669.96, 458703.5 242681.8, 458702.79 242686.6, 458702.1 242691.6, 458701.2 242697.2, 458700.4 242702.8, 458699.7 242707.39, 458698.9 242712.09, 458697.3 242723.59, 458696.39 242729.2, 458695.69 242734.8, 458694.5 242742.19, 458693.19 242749.62, 458692.1 242755.89, 458691.1 242762.18, 458689.8 242768.56, 458688.4 242774.77, 458687 242782.17, 458685.5 242789.67, 458684 242796.77, 458682.6 242803.77, 458681.2 242810.25, 458679.8 242816.67, 458678.7 242822.78, 458677.6 242828.77, 458674.2 242844.36, 458672.7 242850.66, 458671.2 242857.07, 458669.9 242863.67, 458668.4 242870.37, 458667.2 242875.86, 458665.91 242881.35, 458664.51 242886.74, 458663.01 242892.24, 458661.91 242896.54, 458660.61 242900.82, 458658.91 242906.32, 458657.32 242911.71, 458654.31 242921.73, 458652.11 242928.82, 458649.91 242936.02, 458647.81 242942.52, 458645.82 242949.01, 458643.31 242957.12, 458640.91 242965.32, 458635.61 242983.03, 458630.36 243000, 458627.926 243007.903, 458624.683 243006.824, 458622.027 243006.705, 458618.442 243007.965, 458613.65 243009.65, 458616.56 243000, 458617.84 242990.59, 458617.91 242989.74, 458618.86 242979.6, 458620 242969.48, 458626.92 242928, 458637.8 242865.62, 458652.79 242776.23, 458665.06 242701.97, 458672.38 242661.29, 458675.45 242644.55, 458709.44 242642.85))";
        //String geomStr = "POLYGON ((187425.13 897111.27,187426.0 897112.5,187437.0 897121.5,187445.5 897133.5,187467 897152,187476 897162,187486 897174,187494 897183,187504.5 897196.0,187514.0 897209.5,187526.5 897231.5,187542 897264,187550 897278,187564.0 897310.5,187571.5 897321.5,187587.5 897347.5,187605.5 897374.0,187612.5 897386.5,187630.0 897410.5,187632.5 897415.0,187636.5 897420.0,187647.5 897431.0,187656.5 897441.0,187666.0 897451.5,187680.0 897463.5,187701.0 897482.5,187706.5 897489.5,187720 897503,187733.0 897516.5,187739.5 897522.0,187744.5 897531.0,187751.0 897536.5,187759 897541,187766.5 897549.0,187776.0 897559.5,187789.5 897575.5,187807 897597,187817 897613,187817.5 897614.5,187819.5 897622.5,187820 897625,187821 897633,187820.5 897639.0,187819 897649,187813.0 897678.5,187813.42 897690.56,187809 897685,187802.5 897679.5,187787 897663,187773.5 897653.0,187769.5 897650.5,187769 897650,187764 897648,187759.5 897647.0,187759.0 897646.5,187736.5 897643.5,187728.5 897647.5,187726.5 897648.0,187724.5 897650.0,187723.5 897652.0,187723 897654,187723.0 897654.5,187722 897666,187724.88 897675.61,187714.0 897672.5,187710.5 897672.0,187706.5 897672.0,187703.0 897672.5,187699.5 897673.5,187697.5 897674.5,187693.5 897676.0,187688.5 897681.0,187685.0 897688.5,187674.5 897699.5,187671 897707,187661.0 897715.5,187657 897726,187655 897729,187652 897732,187647 897736,187645.5 897737.5,187645 897740,187644.5 897741.0,187644.5 897744.5,187648.5 897759.0,187669.0 897772.5,187685.5 897777.5,187686.5 897779.0,187687 897785,187694.5 897807.5,187701 897821,187710.0 897848.5,187709.5 897857.5,187717.5 897875.0,187719 897882,187710.0 897887.5,187706.5 897888.5,187703 897889,187697 897889,187677.0 897880.5,187671.5 897877.5,187666.5 897874.0,187666.0 897873.5,187661 897869,187657.5 897865.0,187643 897846,187630.0 897829.5,187618.5 897817.5,187609.5 897810.5,187609 897810,187605.5 897806.0,187603.5 897803.5,187599.0 897795.5,187592.0 897787.5,187589.0 897779.5,187586.0 897774.5,187583.5 897771.0,187577.5 897765.0,187565.0 897754.5,187556.0 897747.5,187550.5 897743.5,187549.5 897739.0,187527 897720,187523.5 897715.5,187506.5 897705.0,187497.5 897698.0,187490.5 897694.5,187480.5 897686.0,187450 897669,187435.5 897663.5,187421.04 897659.77,187421.0 897658.5,187423.0 897652.5,187424 897647,187425 897642,187425.0 897613.5,187424.5 897606.5,187424.5 897605.0,187423.5 897598.0,187421.5 897590.5,187420.5 897584.0,187412.5 897566.0,187410.0 897554.5,187399.0 897539.5,187385 897520,187378.5 897513.0,187363.5 897495.5,187356.5 897491.5,187343 897480,187336.5 897475.5,187334.5 897474.0,187328.0 897470.5,187316.5 897460.0,187300 897446,187291.5 897438.5,187287 897428,187278.0 897418.5,187274 897410,187268.0 897401.5,187258.5 897384.0,187255 897376,187249.5 897369.5,187240.0 897356.5,187230 897339,187221 897319,187220 897317,187215.0 897312.5,187214.5 897312.0,187210 897307,187208.5 897305.0,187205 897300,187202.0 897294.5,187201.5 897293.0,187186 897253,187183 897227,187182.0 897206.5,187183.5 897199.0,187182.0 897183.5,187181.5 897170.5,187183.5 897158.0,187183 897148,187185 897138,187184.5 897133.5,187183.5 897129.5,187181.5 897125.5,187179.5 897122.0,187178.5 897120.5,187170.0 897112.5,187166 897110,187163.0 897106.5,187160.5 897103.5,187158.0 897099.5,187156.0 897095.5,187156 897095,187144.59 897073.45,187157 897082,187173 897092,187189.0 897099.5,187204.5 897106.0,187214.5 897110.0,187225 897114,187245.0 897119.5,187255.0 897121.5,187270.5 897123.5,187287.0 897126.5,187304.0 897127.5,187324 897128,187342.0 897127.5,187357 897126,187369.0 897125.5,187372.0 897125.5,187382 897124,187382.5 897124.0,187392.5 897122.0,187402.5 897119.5,187414.5 897114.5,187425.13 897111.27),(187262.0 897335.5,187269 897335,187275.5 897335.5,187288.0 897331.5,187299.5 897320.5,187300.0 897319.5,187303 897315,187305.5 897310.0,187306.0 897309.5,187307.5 897304.5,187309 897299,187309.5 897293.5,187305.5 897279.0,187308.0 897272.5,187305.5 897258.5,187305 897257,187304.0 897253.5,187302.5 897251.0,187301 897248,187298.5 897245.5,187280 897228,187266.5 897215.5,187252 897211,187230 897202,187229.0 897201.5,187227.5 897201.0,187226.0 897201.5,187225.5 897201.5,187224 897202,187223.0 897203.5,187223.0 897216.5,187223.5 897219.5,187225.0 897224.5,187225.5 897226.5,187227.5 897231.0,187238.5 897257.5,187243.0 897275.5,187243.0 897278.5,187242.5 897286.5,187246 897293,187247.0 897299.5,187253.0 897309.5,187252.5 897317.0,187256.5 897326.5,187262.0 897335.5),(187592.5 897684.0,187592.0 897689.5,187592 897692,187593 897694,187593.0 897694.5,187594 897696,187595 897697,187596.0 897697.5,187598.5 897697.5,187603.5 897697.0,187607 897694,187613.5 897689.0,187623.0 897675.5,187630.0 897671.5,187630 897671,187631.5 897670.0,187633.0 897668.5,187633.5 897667.0,187633.5 897665.0,187634.5 897663.0,187637.0 897660.5,187640.0 897655.5,187641 897654,187641 897651,187637.5 897643.5,187626.0 897636.5,187620 897629,187613 897625,187610.5 897621.5,187602.5 897618.0,187601.5 897617.5,187598.0 897616.5,187595.5 897616.0,187591.5 897615.5,187581.5 897620.5,187580 897627,187581.5 897633.5,187582 897641,187587.5 897658.0,187588 897663,187590.5 897667.0,187591.5 897668.0,187592 897670,187592 897672,187591.5 897674.0,187590.5 897681.0,187592.5 897684.0))";
        String geomStr = "POLYGON (( 189290.46 897062.57, 189291.18 897062.54, 189291.72 897062.75, 189292.24 897063.06, 189292.7 897063.29, 189293.32 897063.91, 189293.92 897064.31, 189294.5 897064.88, 189294.92 897065.52, 189294.94 897065.55, 189295.82 897066.83, 189296.65 897068.27, 189297.36 897069.8, 189297.74 897070.75, 189298.07 897071.16, 189298.28 897071.35, 189298.32 897071.43, 189299.8 897070.3, 189297.7 897067.6, 189295.5 897063.8, 189294 897062.9, 189291.8 897061.2, 189290 897061, 189290.46 897062.57 ))";
        WKTReader reader = new WKTReader();
        Geometry geom;
        try {
            boolean hasDualDivisor = true;
            boolean useLowerBound = true;
            geom = reader.read(geomStr);
            // by resolution, no containment test
            //String[][] refs = compBNGRadigResCnt(geom, hasDualDivisor, 5, useLowerBound, 0);
            // by extent
            //String[][] refs = compBNGRadigExtCnt(geom, hasDualDivisor, 3, useLowerBound, 1);
            // adaptive
            //String[][] refs = compBNGRadigAdaptiveCnt(geom, hasDualDivisor, 0, 15, 5, 1, 0.8);
            //String[][] refs = compBNGRadigAdaptiveCnt(geom, hasDualDivisor, 0, 1, 1, 1, 0.8);
            String[][] refs = compBNGRadigAdaptiveCnt(geom, hasDualDivisor, 0, 2, 1.0, 0.00001, 0.8);
            String[] refsInt = compBNGRadigAdaptiveInt(geom, hasDualDivisor, 0, 2,  1, 0.00001, 0.8);
            // num digits
            //String[][] refs = compBNGRadigDigitsCnt(geom, hasDualDivisor, 6, true, true, 1);
            System.out.println((refs[0].length+refs[1].length) + "\n---------");

            for(String ref:refs[0]) {
                String iLRef = convertBNGRadigToILRef(ref);
                String wkt = Radig_DigitInterleaver.iLRefToWKT(iLRef, 10, 7, 2, true);
                System.out.println(wkt);
            }
            System.out.println("****************");
            for(String ref:refs[1]) {
                String iLRef = convertBNGRadigToILRef(ref);
                String wkt = Radig_DigitInterleaver.iLRefToWKT(iLRef, 10, 7, 2, true);
                System.out.println(wkt);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    static void test2(){
        String geomStr = "POINT (377046 538125)";
        WKTReader reader = new WKTReader();
        Geometry geom;
        try {
            boolean hasDualDivisor = true;
            boolean useLowerBound = true;
            geom = reader.read(geomStr);
            String[] refsInt = compPointBNGRadigRefRes((Point) geom,hasDualDivisor, 1.0 );
            for(String ref:refsInt){
                System.out.println(ref);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    static void test3(){
        //String geomStr = "POINT (377046 538125)";
        String geomStr = "POINT (50 500050)";
        WKTReader reader = new WKTReader();
        Geometry geom;
        try {
            boolean hasDualDivisor = false;
            boolean useLowerBound = true;
            geom = reader.read(geomStr);
            //String[] refsInt = compBNGRadigResInt(geom,hasDualDivisor, 1000.0, true, 1);
            String[] refsInt = compBNGRadigAdaptiveInt(geom, false, 0, 2, 1.0, 0.5, 0.5);
            for(String ref:refsInt){
                System.out.println(ref);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    static void test4(){
        double x = 600000.0;
        double y = 0.0;
        String str = Radig_DigitInterleaver.digitInterleaving(x, y, 2, 7, 10, 2, false, true);
        System.out.println(str);
    }
    public static void main(String[] args) {
        test4();
    }
}