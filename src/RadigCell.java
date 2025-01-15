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

package uk.osgb.algorithm.radig2;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * @author SZhou
 *
 */
public class RadigCell {
	Geometry cellGeom = null;
	// interleaved grid reference
	String iLRef = null;
	RadigCell[][] subGrid = null;
	public RadigCell() {}

	/**
	 *
	 * @param minx
	 * @param miny
	 * @param grid
	 * @param radigInfo
	 * @param gf
	 */
	public void reInitCell(double minx, double miny, RadigGrid grid, RadigInfo radigInfo,  GeometryFactory gf) {
		RadigDef radigDef = grid.getRadigDef();
		int base = radigDef.getBase();
		double res = radigInfo.getResolution();
		double maxx = minx + res;
		double maxy = miny + res;
		if(cellGeom == null) {
			Coordinate[] coords = new Coordinate[5];
			coords[0] = new Coordinate(minx, miny);
			coords[1] = new Coordinate(maxx, miny);
			coords[2] = new Coordinate(maxx, maxy);
			coords[3] = new Coordinate(minx, maxy);
			coords[4] = new Coordinate(minx, miny);
			cellGeom = gf.createPolygon(coords);
		}else {
			Coordinate[] coords = cellGeom.getCoordinates();
			coords[0].setX(minx);
			coords[0].setY(miny);
			coords[1].setX(maxx);
			coords[1].setY(miny);
			coords[2].setX(maxx);
			coords[2].setY(maxy);
			coords[3].setX(minx);
			coords[3].setY(maxy);
			coords[4].setX(minx);
			coords[4].setY(miny);
			cellGeom.geometryChanged();
		}
		iLRef = Radig_DigitInterleaver.digitInterleaving(minx, miny, radigInfo.getNumDigits(), radigDef.getNumTotalIntDigits(), base, radigDef.getFirstDivisor(), radigInfo.isEndAtFirstDivisor(), radigDef.isPriorityX());
	}

	/**
	 *
	 * @param grid
	 */
	public void resetCell(RadigGrid grid) {
		if(subGrid!=null) {
			int numRow = subGrid.length;
			int numCol = subGrid[0].length;
			for(int i = 0; i < numRow; ++i) {
				for(int j = 0; j < numCol; ++j) {
					RadigCell cell = subGrid[i][j];
					if(cell!=null) {
						cell.resetCell(grid); // recursively reset cells
						subGrid[i][j] = null;
					}
				}
			}
			subGrid = null;
		}
		if(grid.freeCells.size() < grid.freeCellCapacity) {
			grid.freeCells.addFirst(this);
		}
	}

	/**
	 *
	 * @return
	 */
	public String getGridCellRef() {
		return iLRef;
	}

	/**
	 *
	 * @param curLevel
	 * @param grid
	 * @param gf
	 * @return
	 */
	public String subdivision(int curLevel, RadigGrid grid, GeometryFactory gf) {
		int subDivisionLevel = grid.radigDef.getSubDivisionLevel();
		Envelope env = cellGeom.getEnvelopeInternal();
		double res = env.getWidth();
		if(subDivisionLevel < 0 || curLevel <= subDivisionLevel || res > grid.radigDef.getMinSubDivisionRes()) {// subdivision is allowed										
			RadigInfo info = new RadigInfo();
			double subRes = grid.radigDef.getNextFineResolution(res, info);
			if(subRes > 0) {// not reaching the finest resolution in the range yet
				RadigDef radigDef = grid.getRadigDef();
				int base = radigDef.getBase();
				double minx = env.getMinX();
				double miny = env.getMinY();
				int numCol = (int) Math.rint(res/subRes);
				subGrid = new RadigCell[numCol][numCol];
				for(int i = 0; i < numCol; ++i) {
					for(int j = 0; j < numCol; ++j) {
						if(grid.freeCells.size()>0) {
							subGrid[i][j] = grid.freeCells.pollLast();
						}else {
							subGrid[i][j] = new RadigCell();
						}
						subGrid[i][j].reInitCell(minx + subRes*i, miny + subRes*j, grid, info, gf);
					}
				}
				return null;
			}else {
				return iLRef;
			}
		}else {// no division, just return
			return iLRef;
		}
	}

	/** Test topological relation between the cell geometry and the input geometry
	 *
	 * @param geom the input geometry
	 * @param intOption 1: need to intersect interior; 0: allow boundary intersection
	 * @param curLevel current subdivision level
	 * @param eaRatio effective area ratio to control subdivision of polygon geometry
	 * @param elRatio effective length ratio to control subdivision of linear geometry
	 * @param grid The parent Radig grid
	 * @param intRlts if the cell geometry intersects with input geometry and cntRlts is null, the cell ref will be added to the list for returning result
	 * @param cntRlts if not null and the cell is fully contained by the input geometry, the reference is added to this list instead
	 * @return 1: input geometry intersects or contains cell geometry ; 0: not intersected (or no common interior point if intOption is 1)
	 */
	public int relates(Geometry geom, int intOption, int curLevel, double eaRatio, double elRatio, RadigGrid grid, List<String> intRlts, List<String> cntRlts) {
		int dim = geom.getDimension();
		double res = cellGeom.getEnvelopeInternal().getWidth(); // cell resolution
		String prefix = grid.getRadigPrefix();
		if(dim==2) {// geom is a polygon
			if(cellGeom.intersects(geom)) {
				boolean testContain = (cntRlts == null)?false:true;
				if(testContain) {
					try {
						if(geom.contains(cellGeom)) {// cell is completely inside geom 
							cntRlts.add(prefix+iLRef);
							return 1;
						}
					}catch(Exception e) {
						System.out.println(geom.toText());
						System.out.println(cellGeom.toText());
					}
				}
				Geometry intGeom = cellGeom.intersection(geom);
				if(intGeom.getDimension() <2) {// point or linestring
					if(intOption == 1) {// need to intersect interior
						return 0;
					}else {// intOption == 0, allow boundary intersection
						if(intGeom.getDimension() == 0) {// cellGeom intersects geom at a boundary point
							if(intGeom.getGeometryType().compareTo("Point")==0) {
								double x = intGeom.getCoordinate().x;
								double y = intGeom.getCoordinate().y;
								double intPtRes = grid.radigDef.getResForIntPoint();
								if(intPtRes < 0) {// use num digits, assuming end at seond divisor
									int numDigits = grid.radigDef.getTotalDigitsForIntPoint();
									if(numDigits > 0) {
										return grid.compPointOnCellBoundaryRadigRef(x, y, numDigits, this, intRlts); // full radig ref, no need to add prefix
									}else {
										intPtRes = grid.radigDef.getResLimit(res, curLevel);
										return grid.compPointOnCellBoundaryRadigRef(x, y, intPtRes, this, intRlts);
									}
								}else {
									return grid.compPointOnCellBoundaryRadigRef(x, y, intPtRes, this, intRlts);
								}
							}else {
								System.out.println("Type error: "+intGeom.getGeometryType()+" - should be Point");
							}
						}else {// a linestring
							double ratio = intGeom.getLength() / res;
							if(ratio >= elRatio || (curLevel >= grid.radigDef.getSubDivisionLevel())||(res <= grid.radigDef.getMinSubDivisionRes())) {
								intRlts.add(prefix+iLRef);
								return 1;
							}else {
								String ref = subdivision(curLevel+1, grid, cellGeom.getFactory());
								if(ref!=null) {
									intRlts.add(prefix+iLRef);
									return 1;
								}else {// process subgrid
									return processSubGrid(geom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
									// this is not safe
									//return processSubGrid(intGeom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
								}
							}
						}
					}
				}else {// intGeom is a polygon
					double ratio = intGeom.getArea() / cellGeom.getArea();
					if((ratio >= eaRatio && (cntRlts!=null?curLevel >= grid.radigDef.getSubDivisionLevel():true)) || curLevel >= grid.radigDef.getSubDivisionLevel() ||(res <= grid.radigDef.getMinSubDivisionRes())) {
						intRlts.add(prefix+iLRef);
						return 1;
					}else {// sub division
						String ref = subdivision(curLevel+1, grid, cellGeom.getFactory());
						if(ref!=null) {
							intRlts.add(prefix+iLRef);
							return 1;
						}else {// process subgrid
							return processSubGrid(geom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
							//return processSubGrid(intGeom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
						}
					}
				}
			}else {// does not intersect
				return 0;
			}
		}else if(dim==1) {// geom is linestring
			if(cellGeom.intersects(geom)) {
				if(intOption == 1 && !cellGeom.relate(geom, "T********")) {
					return 0;
				}
				Geometry intGeom = cellGeom.intersection(geom);
				if(intGeom.getDimension() == 0) {// cellGeom intersects geom at a boundary point
					if(intGeom.getGeometryType().compareTo("Point")==0) {
						double x = intGeom.getCoordinate().x;
						double y = intGeom.getCoordinate().y;
						double intPtRes = grid.radigDef.getResForIntPoint();
						if(intPtRes < 0) {// use num digits, assuming end at seond divisor
							int numDigits = grid.radigDef.getTotalDigitsForIntPoint();
							if(numDigits > 0) {
								return grid.compPointOnCellBoundaryRadigRef(x, y, numDigits, this, intRlts); // full radig ref, no need to add prefix
							}else {
								intPtRes = grid.radigDef.getResLimit(res, curLevel);
								return grid.compPointOnCellBoundaryRadigRef(x, y, intPtRes, this, intRlts);
							}
						}else {
							return grid.compPointOnCellBoundaryRadigRef(x, y, intPtRes, this, intRlts);
						}
					}else {
						System.out.println("Type error: "+intGeom.getGeometryType()+" - should be Point");
					}
				}else {// a linestring
					double ratio = intGeom.getLength() / res;
					if((ratio >= elRatio) || (curLevel >= grid.radigDef.getSubDivisionLevel())|| (res <= grid.radigDef.getMinSubDivisionRes())) {
						intRlts.add(prefix+iLRef);
						return 1;
					}else {// sub division
						String ref = subdivision(curLevel+1, grid, cellGeom.getFactory());
						if(ref!=null) {
							intRlts.add(prefix+iLRef);
							return 1;
						}else {// process subgrid
							return processSubGrid(geom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
							//return processSubGrid(intGeom, intOption, curLevel+1, eaRatio, elRatio, grid, intRlts, cntRlts);
						}
					}
				}
			}else {
				return 0;
			}
		}else {// point feature
			double x = geom.getCoordinate().x;
			double y = geom.getCoordinate().y;
			return grid.compPointRadigRef(x, y, res, intRlts);
			//double ptRes = grid.radigDef.getResForPoint();
			//if(ptRes < 0) {// use num digits, assuming end at seond divisor
			//	int numDigits = grid.radigDef.getTotalDigitsForPoint();
			//	return grid.compPointRadigRef(x, y, numDigits, false, intRlts);
			//}else {
			//	return grid.compPointRadigRef(x, y, ptRes, intRlts);
			//}
		}
		return 0;
	}
	/** called recursively in relates(...) to process subdivided grid cells
	 * @param geom input geometry (will it be safe to use intersection-geom rather than the original one? the answer might be no according to our experiment)
	 * @param intOption
	 * @param curLevel
	 * @param eaRatio
	 * @param elRatio
	 * @param grid
	 * @param intRlts
	 * @param cntRlts
	 */
	private int processSubGrid(Geometry geom, int intOption, int curLevel, double eaRatio, double elRatio, RadigGrid grid, List<String> intRlts, List<String> cntRlts) {
		int cnt = 0;
		if(subGrid!=null) {
			int dim = subGrid.length;
			for(int i = 0; i < dim; ++i) {
				for(int j = 0; j < dim; ++j) {
					RadigCell cell = subGrid[i][j];
					int rtn = cell.relates(geom, intOption, curLevel, eaRatio, elRatio, grid, intRlts, cntRlts);
					if(rtn == 0) {// not related, recycle it now
						cell.resetCell(grid);
						subGrid[i][j] = null;
					}else {
						cnt+=rtn;
					}
				}
			}
		}
		return cnt;
	}
	/**
	 * @param geom
	 * @param intOption 0: any intersection; 1: interior intersects; 2: input geom contains cell geometry
	 * @return
	 */
	public String relates(Geometry geom, int intOption) {
		if((intOption == 1 && cellGeom.relate(geom, "T********"))||(intOption == 2 && geom.contains(cellGeom)) || (intOption==0 && cellGeom.intersects(geom))){
			return iLRef;
		}else {
			return null;
		}
	}
	//
	public boolean intersects(Geometry geom) {
		return cellGeom.intersects(geom);
	}
	public boolean isContainedBy(Geometry geom) {
		return geom.contains(cellGeom);
	}
	public boolean intersectsInterior(Geometry geom) {
		return cellGeom.relate(geom, "T********");
	}
}
