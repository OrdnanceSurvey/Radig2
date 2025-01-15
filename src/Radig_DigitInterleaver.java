/** This class contains methods for encoding a coordinate to an interleaved reference string and to decode the strings to coordinates
 *  Currently, the radix (base) supported for string encoding are 2 to 16
 *  for dual divisor systems (a double digit is encoded by two divisors), the possible combinations are any first_div x second_div = base ( 2 < base <=16)
 *
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
package uk.osgb.algorithm.radig2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.union.UnaryUnionOp;

public class Radig_DigitInterleaver {
	static char[] DIGIT_MAPPING = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	static int MAX_DECIMAL_DIGITS = 16;
	
	/** Convert 2D decimal coordinates to a digit interleaving string
	 * @param x X ordinate
	 * @param y Y ordinate
	 * @param numDigits: number of leading digits (including decimal digits) to be encoded in ONE ordinate (the ordinates will be padded with zeros to the length of totalIntDigits)
	 * @param totalIntDigits: total integer digits used for ONE ordinate in the encoding scheme
	 * @param priorityX If true, encode X ordinate first
	 * @return digit interleaved string for the coordinate
	 */
	public static String digitInterleavingDec2D(double x, double y, int numDigits, int totalIntDigits, boolean priorityX) {
		String xStr = Double.toString(x);
		String yStr = Double.toString(y);
		return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);
	}
	/** Convert 2D binary coordinates to a digit interleaving string
	 *
	 * @param x X ordinate
	 * @param y Y ordinate
	 * @param numDigits: number of leading digits (including decimal digits) to be encoded in each ordinate (the ordinates will be padded with zeros to the length of totalIntDigits)
	 * @param totalIntDigits: total integer digits used for an ordinate in the encoding scheme
	 * @param priorityX If true, encode X ordinate first
	 * @return digit interleaved string for the coordinate
	 */
	public static String digitInterleavingBin2D(double x, double y, int numDigits, int totalIntDigits, boolean priorityX) {
		// use integer part only - may not always be able to convert decimal part to binary precisely
		String xStr = Long.toBinaryString(Double.valueOf(x).longValue());
		String yStr = Long.toBinaryString(Double.valueOf(y).longValue());
		return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);		
	}

	/** Convert 2D octal coordinates to a digit interleaving string
	 *
	 * @param x X ordinate
	 * @param y Y ordinate
	 * @param numDigits: number of leading digits (including decimal digits) to be encoded in each ordinate (the ordinates will be padded with zeros to the length of totalIntDigits)
	 * @param totalIntDigits: total integer digits used for an ordinate in the encoding scheme
	 * @param priorityX If true, encode X ordinate first
	 * @return digit interleaved string for the coordinate
	 */
	public static String digitInterleavingOct2D(double x, double y, int numDigits, int totalIntDigits, boolean priorityX) {
		// use integer part only - may not always be able to convert decimal part to octal precisely
		String xStr = Long.toOctalString(Double.valueOf(x).longValue());
		String yStr = Long.toOctalString(Double.valueOf(y).longValue());
		return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);		
	}

	/** Convert 2D hexadecimal coordinates to a digit interleaving string
	 *
	 * @param x X ordinate
	 * @param y Y ordinate
	 * @param numDigits: number of leading digits (including decimal digits) to be encoded in each ordinate (the ordinates will be padded with zeros to the length of totalIntDigits)
	 * @param totalIntDigits: total integer digits used for an ordinate in the encoding scheme
	 * @param priorityX If true, encode X ordinate first
	 * @return digit interleaved string for the coordinate
	 */
	public static String digitInterleavingHex2D(double x, double y, int numDigits, int totalIntDigits, boolean priorityX) {
		// use integer part only, may not always be able to convert decimal part to hexadecimal precisely
		String xStr = Long.toHexString(Double.valueOf(x).longValue());
		String yStr = Long.toHexString(Double.valueOf(y).longValue());
		return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);		
	}

	/** Convert 2D decimal coordinates to a digit interleaving string with dual divisors
	 *
	 * @param x X ordinate
	 * @param y Y ordinate
	 * @param numDigits: number of leading digits (including decimal digits) to be encoded in each ordinate (the ordinates will be padded with zeros to the length of totalIntDigits)
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param secondDivisor
	 * @param endAtFirstDivisor
	 * @param priorityX If true, encode X ordinate first
	 * @return
	 */
	public static String digitInterleavingDec2DDiv(double x, double y, int numDigits, int totalIntDigits, int firstDivisor, int secondDivisor, boolean endAtFirstDivisor, boolean priorityX) {
		String xStr = Double.toString(x);
		String yStr = Double.toString(y);
		return digitInterleavingDiv(xStr, yStr, numDigits, totalIntDigits, 10, firstDivisor, secondDivisor, endAtFirstDivisor, priorityX);
	}

	/**
	 *
 	 * @param x
	 * @param y
	 * @param numDigits
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param secondDivisor
	 * @param endAtFirstDivisor
	 * @param priorityX
	 * @return
	 */
	public static String digitInterleavingOct2DDiv(double x, double y, int numDigits, int totalIntDigits, int firstDivisor, int secondDivisor, boolean endAtFirstDivisor, boolean priorityX) {
		// use integer part only, may not always be able to convert decimal part to binary precisely
		String xStr = Long.toOctalString(Double.valueOf(x).longValue());
		String yStr = Long.toOctalString(Double.valueOf(y).longValue());
		return digitInterleavingDiv(xStr, yStr, numDigits, totalIntDigits, 8, firstDivisor, secondDivisor, endAtFirstDivisor, priorityX);		
	}

	/**
	 *
 	 * @param x
	 * @param y
	 * @param numDigits
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param secondDivisor
	 * @param endAtFirstDivisor
	 * @param priorityX
	 * @return
	 */
	public static String digitInterleavingHex2DDiv(double x, double y, int numDigits, int totalIntDigits, int firstDivisor, int secondDivisor, boolean endAtFirstDivisor, boolean priorityX) {
		// use integer part only, may not always be able to convert decimal part to binary precisely
		String xStr = Long.toHexString(Double.valueOf(x).longValue());
		String yStr = Long.toHexString(Double.valueOf(y).longValue());
		return digitInterleavingDiv(xStr, yStr, numDigits, totalIntDigits, 16, firstDivisor, secondDivisor, endAtFirstDivisor, priorityX);		
	}

	/** generic interleaving, for single divisor only
	 * @param xStr X ordinate in String form
	 * @param yStr Y ordinate in String form
	 * @param numDigits number of leading digits to be encoded
	 * @param totalIntDigits total number of integer digits used in this encoding scheme
	 * @param priorityX
	 * @return
	 */
	private static String digitInterleaving(String xStr, String yStr, int numDigits, int totalIntDigits, boolean priorityX) {
		String xStrPad = padDoubleString(xStr, totalIntDigits, true); // dot is removed here
		String yStrPad = padDoubleString(yStr, totalIntDigits, true);
		int xLen = xStrPad.length();
		int yLen = yStrPad.length();
		StringBuffer buff = new StringBuffer(numDigits*2);
		for(int i = 0; i < numDigits; ++i) {
			char cx = (i < xLen)?xStrPad.charAt(i):'0';
			char cy = (i < yLen)?yStrPad.charAt(i):'0';
			
			if(priorityX) {
				buff.append(cx);
				buff.append(cy);
			}else {
				buff.append(cy);
				buff.append(cx);
			}
		}
		return buff.toString();
	}
	//
	/** generic two-divisor interleaving
	 * @param xStr
	 * @param yStr
	 * @param numDigits
	 * @param totalIntDigits
	 * @param base
	 * @param firstDivisor
	 * @param secondDivisor
	 * @param endAtFirstDivisor
	 * @param priorityX if true, encoding X ordinate first
	 * @return
	 */
	private static String digitInterleavingDiv(String xStr, String yStr, int numDigits, int totalIntDigits, int base, int firstDivisor, int secondDivisor, boolean endAtFirstDivisor, boolean priorityX) {
		if(firstDivisor == base) {
			return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);
		}else if(firstDivisor*secondDivisor!=base) {
			return null;
		}
		String xStrPad = padDoubleString(xStr, totalIntDigits, true);
		String yStrPad = padDoubleString(yStr, totalIntDigits, true);
		int xLen = xStrPad.length();
		int yLen = yStrPad.length();
		StringBuffer buff = new StringBuffer(endAtFirstDivisor?((numDigits-1)*4+2):numDigits*4);
		for(int i = 0; i < numDigits; ++i) {
			long valX1 = 0, valX2 = 0, valY1 = 0, valY2 = 0;
			if(i < xLen ) {
				String c = xStrPad.substring(i, i+1);
				long valX = Long.valueOf(c, base);
				valX1 = valX/secondDivisor;
				valX2 = valX % secondDivisor;
			}
			if(i < yLen) {
				String c = yStrPad.substring(i, i+1);
				long valY = Long.valueOf(c, base);
				valY1 = valY/secondDivisor;
				valY2 = valY % secondDivisor;
				
			}
			if(priorityX) {
				buff.append(valX1);
				buff.append(valY1);
				if(!endAtFirstDivisor || i < numDigits -1) {
					buff.append(valX2);
					buff.append(valY2);
				}
			}else {
				buff.append(valY1);
				buff.append(valX1);
				if(!endAtFirstDivisor || i < numDigits -1) {
					buff.append(valY2);
					buff.append(valX2);
				}
			}
		}
		return buff.toString();
	}
	//
	/**
	 * @param x
	 * @param y
	 * @param numDigits
	 * @param totalIntDigits
	 * @param base
	 * @param firstDivisor
	 * @param endAtFirstDivisor
	 * @param priorityX
	 * @return
	 */
	static String digitInterleaving(double x, double y, int numDigits, int totalIntDigits, int base, int firstDivisor, boolean endAtFirstDivisor, boolean priorityX) {
		String xStr = convertDoubleToString(x, base, MAX_DECIMAL_DIGITS);
		String yStr = convertDoubleToString(y, base, MAX_DECIMAL_DIGITS);
		if(base == firstDivisor) {
			return digitInterleaving(xStr, yStr, numDigits, totalIntDigits, priorityX);
		}else {// two divisors
			int secondDivisor = base / firstDivisor;
			return digitInterleavingDiv(xStr, yStr, numDigits, totalIntDigits, base, firstDivisor, secondDivisor, endAtFirstDivisor, priorityX);
		}
	}
	//
	/** pad a double string in the form of "int.dec" with '0' so that the intergral digits is totalIntDigits
	 * @param str string of double or long, could be of any base
	 * @param totalIntDigits number of digits for integer part (base dependent)
	 * @param removeDot if true, the doc is removed from the returning padded string
	 * @return padded string
	 */
	public static String padDoubleString(String str, int totalIntDigits, boolean removeDot) {
		int len = str.length();
		int dotIdx = str.indexOf('.');
		int numIntDigits = dotIdx >=0?dotIdx:len;
		if(numIntDigits >= totalIntDigits) {
			if(removeDot && dotIdx >=0) {
				return str.substring(0, dotIdx) + str.substring(dotIdx+1);
			}else{
				return str;
			}
		}else {
			int padDigits = totalIntDigits - numIntDigits;
			StringBuffer buff = new StringBuffer(len + padDigits);
			for(int i = 0; i < padDigits; ++i) {
				buff.append('0');
			}
			if(removeDot && dotIdx >=0) {
				buff.append(str.substring(0, dotIdx));
				buff.append(str.substring(dotIdx+1));
			}else {
				buff.append(str);
			}
			return buff.toString();
		}
	}
	//
	/** convert a double number to an int.dec string where int is the integer part and dec is the decimal part, converted to base separately
	 * @param number
	 * @param base
	 * @param numOfDecimalDigits
	 * @return
	 */
	static String convertDoubleToString(double number, int base, int numOfDecimalDigits) {
		if(base == 10) {
			return Double.toString(number);
		}else if(base > 1 && base <= 16){
			return convertIntegerPart(number, base) + convertDecimalPart(number, base, numOfDecimalDigits);
		}else { // not supported
			return null;
		}
	}
	//
	/** convert the integer part of a double nubmer to string
	 * @param number
	 * @param base
	 * @return
	 */
	static String convertIntegerPart(double number, int base) {
		String rlt = "";
		number = Math.floor(number);
		do {
			int mod = (int) (number % base);
			//rlt = mod+rlt;
			rlt = DIGIT_MAPPING[mod]+rlt;
			number = Math.floor(number / base);
		}while(number > 0);
		return rlt;
	}
	
	/** convert the first numOfDecimalDigits digits of the decimal part of a double number to a string in the form of ".xxx..."
	 * @param number
	 * @param numOfDecimalDigits
	 * @param base
	 * @return
	 */
	static String convertDecimalPart(double number, int base, int numOfDecimalDigits) {
		double decimal = number - Math.floor(number);
	    int i=1;
	    String num=".";
	    double temp;
	    while (i<=numOfDecimalDigits && number>0) {
	        decimal=decimal*base;
	        temp=Math.floor(decimal);
        	num+=DIGIT_MAPPING[(int)temp]; // maximum 15 for base 16, no error checking here
	        decimal=decimal-temp;
	        i++;
	    }
	    return num;
	}
	//
	/** compute the number of digits (in base-encode) to represent the given extent in base-10 form
	 * @param extent
	 * @param base
	 * @return
	 */
	public static int calcNumIntDigits(double extent, int base) {
		double num = Math.log10(extent) / Math.log10(base);
		double n = Math.floor(num);
		if(n == num) {
			return (int) n;
		}else {
			return (int) (n+1);
		}
	}

	/** compute the number of digits (in base-encode) to represent the give number of base-10 decimal digits
	 * @param numDecDigitBase10
	 * @param base
	 * @return
	 */
	public static int calcNumDecDigits(int numDecDigitBase10, int base) {
		double res = Math.pow(base, -numDecDigitBase10);
		double num = -Math.log10(res) / Math.log10(base);
		double n = Math.floor(num);
		if(n == num) {
			return (int)n;
		}else {
			return (int)(n+1);
		}
		
	}	
	/** Calculate the resolution (cell size) for numDigits digits representation
	 * @param base radix of the number system for ordinate values
	 * @param numDigits total number of digits to be used to encode (integral + decimal)
	 * @param totalIntDigits total number of integer digits for the grid system
	 * @param firstDivisor for digit sub-division. If 0, no subdivision and the base is used as the only divisor; otherwise: base = firstDivisor X secondDivisor
	 * @return
	 */
	public static double calcResolution(int base, int numDigits, int totalIntDigits, int firstDivisor, boolean endAtFirstDivisor) {
		if(firstDivisor == base || firstDivisor <= 0) {
			return Math.pow(base, totalIntDigits - numDigits);
		}else {
			double res =  Math.pow(base, totalIntDigits - numDigits);
			if(endAtFirstDivisor) {
				res *=base/firstDivisor;
			}
			return res;
		}
	}
	//
	/** calculate the resolution from an interleaving reference
	 * @param iLRef Interleaved reference string
	 * @param base the radix used to generate the reference string
	 * @param totalIntDigits total integer digits used in the original coordinate system for reference encoding
	 * @param firstDivisor
	 * @return
	 */
	public static double calcResolutionILRef(String iLRef, int base, int totalIntDigits, int firstDivisor) {
		boolean hasDualDivisor = base != firstDivisor;
		int len = iLRef.length();
		int totalDigits = hasDualDivisor?len/4:len/2;
		boolean endAtFirstDivisor = false;
		if(hasDualDivisor && len % 4 == 2) {
			totalDigits+=1;
			endAtFirstDivisor = true;
		}
		return calcResolution(base, totalDigits, totalIntDigits, firstDivisor, endAtFirstDivisor);
	}

	/**
	 *
	 * @param iLRef inter-leaved reference string to be converted to coordinate string
	 * @param base
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 * @param rlt decoded coordinate value is set to this argument
	 * @return resolution of the interleaved reference string
	 */
	public static double iLRefToCoordStrings(String iLRef, int base, int totalIntDigits, int firstDivisor, boolean priorityX, Coordinate rlt) {

		boolean hasDualDivisor = base != firstDivisor;
		int len = iLRef.length();
		int totalDigits = hasDualDivisor?len/4:len/2;
		boolean endAtFirstDivisor = false;
		if(hasDualDivisor && len % 4 == 2) {
			endAtFirstDivisor = true;
			totalDigits+=1;
		}
		int secondDivisor = base/firstDivisor;
		int intDigits = totalDigits > totalIntDigits?totalIntDigits:totalDigits;
		int decDigits = totalDigits > totalIntDigits? totalDigits - totalIntDigits: 0;
		
		double val1 = 0.0;
		double val2 = 0.0;
		double res = 0.0;
		if(hasDualDivisor) {
			for(int i = 0; i <totalDigits; ++i) {
				int j = i*4;
				res = secondDivisor*Math.pow(base, totalIntDigits-i-1);
				String digit1 = iLRef.substring(j, j+1);
				val1 += Integer.valueOf(digit1)*res;
				j+=1;
				String digit2 = iLRef.substring(j, j+1);
				val2 += Integer.valueOf(digit2)*res;
				j+=1;
				if(j<len) {// check for end at first divisor
					res = Math.pow(base, totalIntDigits-i-1);
					digit1 = iLRef.substring(j, j+1);
					val1+= Integer.valueOf(digit1)*res;
					j+=1;
					digit2 = iLRef.substring(j, j+1);
					val2 += Integer.valueOf(digit2)*res;
				}
			}
		}else {
			for(int i = 0; i <totalDigits; ++i) {
				res = Math.pow(base, totalIntDigits-i-1);
				int j = i*2;
				String digit1 = iLRef.substring(j, j+1);
				val1 += Integer.valueOf(digit1)*res;
				j+=1;
				String digit2 = iLRef.substring(j, j+1);
				val2 += Integer.valueOf(digit2)*res;
			}
		}
		rlt.x = priorityX?val1:val2;
		rlt.y = priorityX?val2:val1;
		return res;
	}

	/** generate the geometry for the grid cell represented by the interleaved reference string (the decoded coordinate is the bottom left corner of the grid cell)
	 *
	 * @param iLRef
	 * @param base
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 * @return
	 */
	public static Geometry iLRefToGeometry(String iLRef, int base, int totalIntDigits, int firstDivisor, boolean priorityX) {
		Coordinate coord = new Coordinate();
		double res = iLRefToCoordStrings(iLRef, base, totalIntDigits, firstDivisor, priorityX, coord);
		double minx = coord.x;
		double miny = coord.y;
		double maxx = minx + res;
		double maxy = miny +res;
		Coordinate[] coords = new Coordinate[5];
		coords[0] = new Coordinate(minx, miny);
		coords[1] = new Coordinate(maxx, miny);
		coords[2] = new Coordinate(maxx, maxy);
		coords[3] = new Coordinate(minx, maxy);
		coords[4] = new Coordinate(minx, miny);
		GeometryFactory gf = new GeometryFactory();
		return gf.createPolygon(coords);
	}

	/** generate the geometry for the grid cell represented by the interleaved reference string and return the WKT of the cell geometry
	 *
	 * @param iLRef
	 * @param base
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 * @return
	 */
	public static String iLRefToWKT(String iLRef, int base, int totalIntDigits, int firstDivisor, boolean priorityX) {
		return iLRefToGeometry(iLRef, base, totalIntDigits, firstDivisor, priorityX).toText();
	}

	/** display the WKT of the grid cells represented by a collection of interleaved reference strings (for debugging)
	 *
	 * @param refs
	 * @param base
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 */
	public static void refsToWKT(Collection<String> refs, int base, int totalIntDigits, int firstDivisor, boolean priorityX) {
		for(String ref:refs) {
			String wkt = iLRefToGeometry(ref, base, totalIntDigits, firstDivisor, priorityX).toText();
			System.out.println(wkt);
		}
	}

	/** display the WKT of the grid cells represented by an array of interleaved reference strings (for debugging)
	 *
	 * @param refs
	 * @param base
	 * @param totalIntDigits
	 * @param firstDivisor
	 * @param priorityX
	 */
	public static void refsToWKT(String[] refs, int base, int totalIntDigits, int firstDivisor, boolean priorityX) {
		for(String ref:refs) {
			String wkt = iLRefToGeometry(ref, base, totalIntDigits, firstDivisor, priorityX).toText();
			System.out.println(wkt);
		}
	}
	//
	public static void main(String[] args) {
//		String  c = "1";
//		int val = Integer.valueOf(c);
//		System.out.println(val);
//				
//		double res = calcResolution(10, 1, 7, 2, false);
//		System.out.println(res);
//		res = calcResolution(10, 1, 7, 2, true);
//		System.out.println(res);
		test4();
	}
	public static void test() {
		//RadigGrid grid = new RadigGrid(2, 21, 32, 2, true);
		//RadigGrid grid = new RadigGrid(10, 7, 10, 2, true);
//		int numIntDigits = calcNumIntDigits(1300000, 2);
//		int numDecDigits = calcNumDecDigits(3, 2);
//		RadigGrid grid = new RadigGrid(2, numIntDigits, numIntDigits + numDecDigits, 2, true);
		int numIntDigits = calcNumIntDigits(1300000, 16);
		int numDecDigits = calcNumDecDigits(3, 16);
		RadigGrid grid = new RadigGrid(16, numIntDigits, numIntDigits + numDecDigits, 4, true);
		
		String g1 = "Polygon((30 20, 80 30, 130 20, 180 60, 160 200, 130 210, 90 200, 70 200, 40 170, 10 80, 30 20))";
		String g2 = "LineString (30 20, 30 80)";
		String g3 = "POLYGON ((187425.13 897111.27,187426.0 897112.5,187437.0 897121.5,187445.5 897133.5,187467 897152,187476 897162,187486 897174,187494 897183,187504.5 897196.0,187514.0 897209.5,187526.5 897231.5,187542 897264,187550 897278,187564.0 897310.5,187571.5 897321.5,187587.5 897347.5,187605.5 897374.0,187612.5 897386.5,187630.0 897410.5,187632.5 897415.0,187636.5 897420.0,187647.5 897431.0,187656.5 897441.0,187666.0 897451.5,187680.0 897463.5,187701.0 897482.5,187706.5 897489.5,187720 897503,187733.0 897516.5,187739.5 897522.0,187744.5 897531.0,187751.0 897536.5,187759 897541,187766.5 897549.0,187776.0 897559.5,187789.5 897575.5,187807 897597,187817 897613,187817.5 897614.5,187819.5 897622.5,187820 897625,187821 897633,187820.5 897639.0,187819 897649,187813.0 897678.5,187813.42 897690.56,187809 897685,187802.5 897679.5,187787 897663,187773.5 897653.0,187769.5 897650.5,187769 897650,187764 897648,187759.5 897647.0,187759.0 897646.5,187736.5 897643.5,187728.5 897647.5,187726.5 897648.0,187724.5 897650.0,187723.5 897652.0,187723 897654,187723.0 897654.5,187722 897666,187724.88 897675.61,187714.0 897672.5,187710.5 897672.0,187706.5 897672.0,187703.0 897672.5,187699.5 897673.5,187697.5 897674.5,187693.5 897676.0,187688.5 897681.0,187685.0 897688.5,187674.5 897699.5,187671 897707,187661.0 897715.5,187657 897726,187655 897729,187652 897732,187647 897736,187645.5 897737.5,187645 897740,187644.5 897741.0,187644.5 897744.5,187648.5 897759.0,187669.0 897772.5,187685.5 897777.5,187686.5 897779.0,187687 897785,187694.5 897807.5,187701 897821,187710.0 897848.5,187709.5 897857.5,187717.5 897875.0,187719 897882,187710.0 897887.5,187706.5 897888.5,187703 897889,187697 897889,187677.0 897880.5,187671.5 897877.5,187666.5 897874.0,187666.0 897873.5,187661 897869,187657.5 897865.0,187643 897846,187630.0 897829.5,187618.5 897817.5,187609.5 897810.5,187609 897810,187605.5 897806.0,187603.5 897803.5,187599.0 897795.5,187592.0 897787.5,187589.0 897779.5,187586.0 897774.5,187583.5 897771.0,187577.5 897765.0,187565.0 897754.5,187556.0 897747.5,187550.5 897743.5,187549.5 897739.0,187527 897720,187523.5 897715.5,187506.5 897705.0,187497.5 897698.0,187490.5 897694.5,187480.5 897686.0,187450 897669,187435.5 897663.5,187421.04 897659.77,187421.0 897658.5,187423.0 897652.5,187424 897647,187425 897642,187425.0 897613.5,187424.5 897606.5,187424.5 897605.0,187423.5 897598.0,187421.5 897590.5,187420.5 897584.0,187412.5 897566.0,187410.0 897554.5,187399.0 897539.5,187385 897520,187378.5 897513.0,187363.5 897495.5,187356.5 897491.5,187343 897480,187336.5 897475.5,187334.5 897474.0,187328.0 897470.5,187316.5 897460.0,187300 897446,187291.5 897438.5,187287 897428,187278.0 897418.5,187274 897410,187268.0 897401.5,187258.5 897384.0,187255 897376,187249.5 897369.5,187240.0 897356.5,187230 897339,187221 897319,187220 897317,187215.0 897312.5,187214.5 897312.0,187210 897307,187208.5 897305.0,187205 897300,187202.0 897294.5,187201.5 897293.0,187186 897253,187183 897227,187182.0 897206.5,187183.5 897199.0,187182.0 897183.5,187181.5 897170.5,187183.5 897158.0,187183 897148,187185 897138,187184.5 897133.5,187183.5 897129.5,187181.5 897125.5,187179.5 897122.0,187178.5 897120.5,187170.0 897112.5,187166 897110,187163.0 897106.5,187160.5 897103.5,187158.0 897099.5,187156.0 897095.5,187156 897095,187144.59 897073.45,187157 897082,187173 897092,187189.0 897099.5,187204.5 897106.0,187214.5 897110.0,187225 897114,187245.0 897119.5,187255.0 897121.5,187270.5 897123.5,187287.0 897126.5,187304.0 897127.5,187324 897128,187342.0 897127.5,187357 897126,187369.0 897125.5,187372.0 897125.5,187382 897124,187382.5 897124.0,187392.5 897122.0,187402.5 897119.5,187414.5 897114.5,187425.13 897111.27),(187262.0 897335.5,187269 897335,187275.5 897335.5,187288.0 897331.5,187299.5 897320.5,187300.0 897319.5,187303 897315,187305.5 897310.0,187306.0 897309.5,187307.5 897304.5,187309 897299,187309.5 897293.5,187305.5 897279.0,187308.0 897272.5,187305.5 897258.5,187305 897257,187304.0 897253.5,187302.5 897251.0,187301 897248,187298.5 897245.5,187280 897228,187266.5 897215.5,187252 897211,187230 897202,187229.0 897201.5,187227.5 897201.0,187226.0 897201.5,187225.5 897201.5,187224 897202,187223.0 897203.5,187223.0 897216.5,187223.5 897219.5,187225.0 897224.5,187225.5 897226.5,187227.5 897231.0,187238.5 897257.5,187243.0 897275.5,187243.0 897278.5,187242.5 897286.5,187246 897293,187247.0 897299.5,187253.0 897309.5,187252.5 897317.0,187256.5 897326.5,187262.0 897335.5),(187592.5 897684.0,187592.0 897689.5,187592 897692,187593 897694,187593.0 897694.5,187594 897696,187595 897697,187596.0 897697.5,187598.5 897697.5,187603.5 897697.0,187607 897694,187613.5 897689.0,187623.0 897675.5,187630.0 897671.5,187630 897671,187631.5 897670.0,187633.0 897668.5,187633.5 897667.0,187633.5 897665.0,187634.5 897663.0,187637.0 897660.5,187640.0 897655.5,187641 897654,187641 897651,187637.5 897643.5,187626.0 897636.5,187620 897629,187613 897625,187610.5 897621.5,187602.5 897618.0,187601.5 897617.5,187598.0 897616.5,187595.5 897616.0,187591.5 897615.5,187581.5 897620.5,187580 897627,187581.5 897633.5,187582 897641,187587.5 897658.0,187588 897663,187590.5 897667.0,187591.5 897668.0,187592 897670,187592 897672,187591.5 897674.0,187590.5 897681.0,187592.5 897684.0))";
//		String g2 = "Polygon((0.5 0, 1.5 0, 1.5 1, 0.5 1, 0.5 0))";
//		String g3 = "Polygon((0 0.5, 1 0.5, 1 1.5, 0 1.5, 0 0.5))";
//		String g4 = "Polygon((2 2, 3 2, 3 3, 2 3, 2 2))";
		WKTReader reader = new WKTReader();
		try {
			//Geometry geom1 = reader.read(g1);
			Geometry geom1 = reader.read(g3);
//			Geometry geom3 = reader.read(g3);
//			Geometry geom4 = reader.read(g4);
			List<String> intRlts = new LinkedList<String>(); 
			List<String> cntRlts = new LinkedList<String>();
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 3, 0.8, 0.8, intRlts, cntRlts);
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 3, 0.8, 0.8, intRlts, cntRlts);
			//int numRefs = grid.compRadigRefAdaptive(geom1, 0, 6, 0.8, 0.8, intRlts, null); 
			int numRefs = grid.compRadigRefAdaptive(geom1, 0, 4, 0.8, 0.8, intRlts, null);
			System.out.println(geom1.toText());
			//refsToWKT(intRlts, 10, 7, 2, true);
			//refsToWKT(intRlts, 2, numIntDigits, 2, true);
			refsToWKT(intRlts, 16, numIntDigits, 4, true);
			System.out.println("---------------");
			if(cntRlts!=null) {
				//refsToWKT(cntRlts, 10, 7, 2, true);
				//refsToWKT(cntRlts, 2, numIntDigits, 2, true);
				refsToWKT(cntRlts, 16, numIntDigits, 4, true);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void test2() {
		String str1 = "135791";
		String str2 = "1368024";
		System.out.println(str1);
		System.out.println(str2);
		String intStrDiv = digitInterleavingDiv(str1, str2, 7, 7, 10, 2, 5, true, true);
		System.out.println(intStrDiv);
		Coordinate coord = new Coordinate();
		double res=iLRefToCoordStrings(intStrDiv, 10, 7, 2, true, coord);
		System.out.println(res+ " - "+coord.x + ", "+coord.y);
		String intStrDiv2 = digitInterleavingDiv(str1, str2, 7, 7, 10, 2, 5, false, true);
		System.out.println(intStrDiv2);
		res = iLRefToCoordStrings(intStrDiv2, 10, 7, 2, true, coord);
		System.out.println(res+ " - "+coord.x + ", "+coord.y);
		
		intStrDiv = digitInterleavingDiv(str1, str2, 6, 7, 10, 10, 1, true, true);
		System.out.println(intStrDiv);
		res=iLRefToCoordStrings(intStrDiv, 10, 7, 10, true, coord);
		System.out.println(res+ " - "+coord.x + ", "+coord.y);
		
		intStrDiv2 = digitInterleavingDiv(str1, str2, 5, 7, 10, 10, 1, false, true);
		System.out.println(intStrDiv2);
		res=iLRefToCoordStrings(intStrDiv2, 10, 7, 10, true, coord);
		System.out.println(res+ " - "+coord.x + ", "+coord.y);

		intStrDiv = digitInterleavingDiv(str1, str2, 5, 7, 10, 2, 5, true, true);
		System.out.println(intStrDiv);
		intStrDiv2 = digitInterleavingDiv(str1, str2, 5, 7, 10, 2, 5, false, true);
		System.out.println(intStrDiv2);

		String intStr = digitInterleaving(str1, str2, 1, 1, true);
		System.out.println(intStr);
		intStrDiv = digitInterleavingDiv(str1, str2, 2, 2, 10, 2, 5, true, true);
		System.out.println(intStrDiv);
		intStrDiv2 = digitInterleavingDiv(str1, str2, 2, 2, 10, 2, 5, false, true);
		System.out.println(intStrDiv2);
		intStr = digitInterleaving(str1, str2, 2, 2, true);
		System.out.println(intStr);
	}
	public static void test3() {
		String ref = "DNG113411420020114210";
		double res = Radig_BNG.calcResBNGRadig(ref);
		String iLRef = Radig_BNG.convertBNGRadigToILRef(ref);
		Geometry geom = iLRefToGeometry(iLRef, 10, 7, 2, true);
		System.out.println(geom.toText());
		System.out.println(res);
	}
	static void test4(){
		double x =  600000.0;
		double y = 1200000.0;
		String str = Radig_DigitInterleaver.digitInterleaving(x, y, 2, 7, 10, 2, false, true);
		System.out.println(str);
	}
}
