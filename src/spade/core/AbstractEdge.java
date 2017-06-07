/*
 --------------------------------------------------------------------------------
 SPADE - Support for Provenance Auditing in Distributed Environments.
 Copyright (C) 2015 SRI International

 This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --------------------------------------------------------------------------------
 */
package spade.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * This is the class from which other edge classes (e.g., OPM edges) are
 * derived.
 *
 * @author Dawood Tariq
 */
public abstract class AbstractEdge implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5777793863959971982L;
	/**
     * A map containing the annotations for this edge.
     */
    protected Map<String, String> annotations = new HashMap<>();
    private AbstractVertex childVertex;
    private AbstractVertex parentVertex;

    /**
     * Returns the map containing the annotations for this edge.
     *
     * @return The map containing the annotations.
     */
    public final Map<String, String> getAnnotations() {
        return annotations;
    }

    /**
     * Adds an annotation.
     *
     * @param key The annotation key.
     * @param value The annotation value.
     */
    public final void addAnnotation(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        annotations.put(key, value);
    }

    /**
     * Adds a map of annotation.
     *
     * @param newAnnotations New annotations to be added.
     */
    public final void addAnnotations(Map<String, String> newAnnotations) {
        for (Map.Entry<String, String> currentEntry : newAnnotations.entrySet()) {
            String key = currentEntry.getKey();
            String value = currentEntry.getValue();
            addAnnotation(key, value);
        }
    }

    /**
     * Removes an annotation.
     *
     * @param key The annotation key to be removed.
     * @return The annotation that is removed, or null of no such annotation key
     * existed.
     */
    public final String removeAnnotation(String key) {
        return annotations.remove(key);
    }

    /**
     * Gets an annotation.
     *
     * @param key The annotation key.
     * @return The value of the annotation corresponding to the key.
     */
    public final String getAnnotation(String key) {
        return annotations.get(key);
    }

    /**
     * Gets the type of this edge.
     *
     * @return A string indicating the type of this edge.
     */
    public final String type() {
        return annotations.get("type");
    }

    // The following functions that get and set source and destination vertices
    // are left empty in this abstract class - they are overridden and implemented
    // in derived classes since the source and destination vertex types may be
    // specific to those classes.
    /**
     * Gets the source vertex.
     *
     * @return The source vertex attached to this edge.
     */
    public final AbstractVertex getChildVertex() {
        return childVertex;
    }

    /**
     * Gets the destination vertex.
     *
     * @return The destination vertex attached to this edge.
     */
    public final AbstractVertex getParentVertex() {
        return parentVertex;
    }

    /**
     * Sets the source vertex.
     *
     * @param childVertex The vertex that is to be set as the source for this
     * edge.
     */
    public final void setChildVertex(AbstractVertex childVertex) {
        this.childVertex = childVertex;
    }

    /**
     * Sets the destination vertex.
     *
     * @param parentVertex The vertex that is to be set as the destination
     * for this edge.
     */
    public final void setParentVertex(AbstractVertex parentVertex) {
        this.parentVertex = parentVertex;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof AbstractEdge)) {
            return false;
        }
        AbstractEdge thatEdge = (AbstractEdge) thatObject;
        return (this.annotations.equals(thatEdge.annotations)
                && this.getChildVertex().equals(thatEdge.getChildVertex())
                && this.getParentVertex().equals(thatEdge.getParentVertex()));
    }

    /**
     * Computes a function of the annotations in the edge and the vertices it is incident upon.
     *
     * This takes less time to compute than bigHashCode() but is less collision-resistant.
     *
     * @return An integer-valued hash code.
     */
    @Override
    public int hashCode() {
        final int seed1 = 5;
        final int seed2 = 97;
        int hashCode = seed1;
        hashCode = seed2 * hashCode + (this.annotations != null ? this.annotations.hashCode() : 0);
        hashCode = seed2 * hashCode + (this.childVertex != null ? this.childVertex.hashCode() : 0);
        hashCode = seed2 * hashCode + (this.parentVertex != null ? this.parentVertex.hashCode() : 0);
        return hashCode;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> currentEntry : annotations.entrySet()) {
            result.append(currentEntry.getKey());
            result.append(":");
            result.append(currentEntry.getValue());
            result.append("|");
        }
        return result.substring(0, result.length() - 1);
    }
    
    /**
     * Computes MD5 of the annotations in the edge and the vertices it is incident upon.
     *
     * This takes longer to compute than hashCode() but is more collision-resistant.
     *
     @return A 128-bit hash value.
     */
    public byte[] bigHashCode() {
        StringBuilder annotations = new StringBuilder();
        annotations.append(this.childVertex != null ? this.childVertex.toString() : "");
        annotations.append(this.toString());
        annotations.append(this.parentVertex != null ? this.parentVertex.toString() : "");
        return DigestUtils.md5(annotations.toString());
    }
}
