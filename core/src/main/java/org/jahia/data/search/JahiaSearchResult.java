/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
// DJ       03.01.2001
// NK		06.05.2002 Added BitSet and accessors addHit(), results(), getHitCount(), bits()
//
//
package org.jahia.data.search;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.services.search.JahiaSearchResultBuilder;
import org.jahia.services.search.ParsedObject;
import org.jahia.utils.OrderedBitSet;

/**
 * An instance of this class is returned by the SearchEngine (and the corresponding search
 * service). It contains a List of JahiaSearchHit and a hit counter.
 *
 * @see org.jahia.engines.search.Search_Engine
 * @author DJ
 * @author NK
 */
public class JahiaSearchResult
{

    /**
     * The BitSet of matching ids, the id could by page id, container id depending
     * of the search level
     */
    private OrderedBitSet bits = new OrderedBitSet();
    private boolean useBitSet = true;
    private boolean hitsIsValid = true;
    private boolean pageHitsMapIsValid = true;
    private boolean fileHitsMapIsValid = true;
    private boolean parsedObjectIsValid = true;
    private JahiaSearchResultBuilder searchResultBuilder;

    /**
     * List of JahiaSearchHit
     */
    private List<JahiaSearchHit> results = new ArrayList<JahiaSearchHit>();

    /**
     * The collection of raw parsedObject instance
     *
     */
    private Collection<ParsedObject> parsedObjects = new ArrayList<ParsedObject>();
    private Map<Integer, List<JahiaSearchHit>> pageHitsMap = new HashMap<Integer, List<JahiaSearchHit>>();
    private Map<String, List<JahiaSearchHit>> fileHitsMap = new HashMap<String, List<JahiaSearchHit>>();

    /**
     * number of hits     
     */
    private int hitcount ;

    public JahiaSearchResult(JahiaSearchResultBuilder searchResultBuilder){
        this.searchResultBuilder = searchResultBuilder;
    }

    public JahiaSearchResult(JahiaSearchResultBuilder searchResultBuilder, boolean useBitSet){
        this(searchResultBuilder);
        this.useBitSet = useBitSet;
    }

    public JahiaSearchResult(JahiaSearchResultBuilder searchResultBuilder,
                             Collection<ParsedObject> parsedObjects){
        this.searchResultBuilder = searchResultBuilder;
        if ( parsedObjects != null ){
            this.parsedObjectIsValid = true;
            this.parsedObjects = parsedObjects;
        }
    }

    /**
     * This check is required prior to using the search result in case of softreferenced internal hits
     * are garbaged by the jvm
     *
     * @return
     */
    public boolean isValid() {
        return ( hitsIsValid && pageHitsMapIsValid && fileHitsMapIsValid && parsedObjectIsValid );
    }

    //--------------------------------------------------------------------------
    /**
     * Add a hit and use the hit's id to set the corresponding bit in the BitSet to true.
     * Increment the hitcount counter.
     *
     * @param hit
     */
    public void addHit(JahiaSearchHit hit){
        if ( hit == null )
            return;

        List<JahiaSearchHit> v = this.results();
        v.add(hit);
        if ( this.useBitSet && hit.getIntegerID() != -1){
            try {
                bits.set(hit.getIntegerID());
            } catch ( Exception t ){
            }
        }
        hitcount++;
    }

    public void removeHit(int index){
        List<JahiaSearchHit> v = this.results();
        if ( v == null ){
            return;
        }
        JahiaSearchHit hit = v.get(index);
        if ( hit != null ){
            v.remove(index);
            hitcount--;
            if ( this.useBitSet ){
                try {
                    bits.clear(hit.getIntegerID());
                } catch ( Exception t ){
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the results.
     *
     * @return List results, the results.
     */
    public List<JahiaSearchHit> results(){
        List<JahiaSearchHit> v = this.results;
        if (v == null){
            v = new ArrayList<JahiaSearchHit>();
            this.results = v;
            this.hitsIsValid = false;
        }
        return v;
    }

    public void setResult(List<JahiaSearchHit> results){
        if ( results != null ){
            hitsIsValid = true;
        }
        this.results = results;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the BitSet of matching ids.
     *
     * @return BitSet the bit set.
     */
    public BitSet bits(){
        return bits;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the collection of raw ParsedObject.
     *
     * @return List results, the results.
     */
    public Collection<ParsedObject> parsedObjects(){
        return parsedObjects;
    }

    //--------------------------------------------------------------------------
    /**
     *
     * @param parsedObjects Collection
     */
    public void setParsedObjects(Collection<ParsedObject> parsedObjects){
        this.parsedObjectIsValid = true;
        this.parsedObjects = parsedObjects;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the number of hits
     *
     * @return int  hitcount, the umber of hits.
     */
    public int getHitCount(){
        return hitcount;
    }

    public JahiaSearchResultBuilder getSearchResultBuilder() {
        return searchResultBuilder;
    }

    public void setSearchResultBuilder(JahiaSearchResultBuilder searchResultBuilder) {
        this.searchResultBuilder = searchResultBuilder;
    }

    public void sort(JahiaSearchResultSorter[] sorters){
        for (JahiaSearchResultSorter sorter : sorters){
            sorter.sort(this);
        }
    }

    public boolean isUseBitSet() {
        return useBitSet;
    }

    public void setUseBitSet(boolean useBitSet) {
        this.useBitSet = useBitSet;
    }

    public Map<Integer, List<JahiaSearchHit>> getPageHitsMap() {
        Map<Integer, List<JahiaSearchHit>> result = pageHitsMap;
        if ( result == null ){
            result = new HashMap<Integer, List<JahiaSearchHit>>();
            pageHitsMap = result;
            this.pageHitsMapIsValid = false;
        }
        return result;
    }

    public void setPageHitsMap(Map<Integer, List<JahiaSearchHit>> pageHitsMap) {
        this.pageHitsMapIsValid = true;
        this.pageHitsMap = pageHitsMap;
    }

    public Map<String, List<JahiaSearchHit>> getFileHitsMap() {
        Map<String, List<JahiaSearchHit>> result = fileHitsMap;
        if ( result == null ){
            result = new HashMap<String, List<JahiaSearchHit>>();
            fileHitsMap = result;
            this.fileHitsMapIsValid = false;
        }
        return result;
    }

    public void setFileHitsMap(Map<String, List<JahiaSearchHit>> fileHitsMap) {
        this.fileHitsMapIsValid = true;
        this.fileHitsMap = fileHitsMap;
    }

}
