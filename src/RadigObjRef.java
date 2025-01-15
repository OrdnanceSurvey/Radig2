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
/** used in RadigMatchTrie for maintaining mapping between radig references and objects (with unique ID and geometry)
 *
 */
package uk.osgb.algorithm.radig2;

//
public class RadigObjRef implements Comparable {
    String id;
    Object obj;

    public RadigObjRef(String id, Object obj) {
        this.id = id;
        this.obj = obj;
    }
    @Override
    public int compareTo(Object o) {
        return this.id.compareTo(((RadigObjRef) o).id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
