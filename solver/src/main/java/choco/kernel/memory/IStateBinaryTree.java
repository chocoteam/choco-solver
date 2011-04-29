/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Ecole des Mines de Nantes nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.memory;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.memory.trailing.trail.StoredBinaryTreeTrail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 25, 2008
 * Time: 9:42:15 AM
 * Interface for a backtrackable binary tree of integer intervals
 */
public interface IStateBinaryTree extends Serializable{

	Logger LOGGER = LoggerFactory.getLogger(IStateBinaryTree.class);
	/**
     * static integer representing an operation over a binary tree
     * INF, a lower bound of an interval has been modified
     */
    int INF = 0;

    /**
     * static integer representing an operation over a binary tree
     * SUP, an upper bound of an interval has been modified
     */
    int SUP = 1;

    /**
     * static integer representing an operation over a binary tree
     * ADD, a new interval has been added in the tree
     */
    int ADD = 2;

    /**
     * static integer representing an operation over a binary tree
     * REM, an interval has been removed from the tree
     */
    int REM = 3;


    /**
     * Inner class representing a node in the binary tree
     * a node is an interval
     */
    class Node {

        /**
         * the lower bound of the node
         */
        public int inf;

        /**
         * the upper bound of the node
         */
        public int sup;

        /**
         * the timestamp of the lwer bound
         */
        public int infStamp;

        /**
         * the timestamp of the upper bound
         */
        public int supStamp;

        /**
         * the binary tree the node belongs to
         */
        public IStateBinaryTree tree;

        /**
         * the father of this node in the tree
         */
        public Node father;

        /**
         * the left son of this node in the tree
         */
        public Node leftNode;

        /**
         * the right son of this node in the tree
         */
        public Node rightNode;

        protected final StoredBinaryTreeTrail myTrail;

        /**
         * Construct a new node (interval)
         * @param tree the tree the node belongs to
         * @param inf the lower bound of this interval
         * @param sup the upper bound of this interval
         */
        public Node(IStateBinaryTree tree, int inf, int sup)
        {
            this.tree = tree;
            this.inf = inf;
            this.sup = sup;
            infStamp = tree.getEnvironment().getWorldIndex();
            supStamp = tree.getEnvironment().getWorldIndex();
            this.myTrail = tree.getEnvironment().getBinaryTreeTrail();
        }

        /**
         * Check wether a value is in the interval represented by thiis node
         * @param value integer value to be check
         * @return true if the value is in the interval, false otherwise
         */
        public boolean contains(int value)
        {
            return value >= inf && value <= sup;
        }

        /**
         * gets the size of the interval represented by this node
         * @return integer value of the size
         */
        public int getSize()
        {
            return sup-inf+1;
        }

        /**
         * set a new lower bound to the node, without saving it
         * used for trailing purpose
         * @param newInf the new value of the lower bound
         * @param infStamp the new timestamp of the lower bound
         */
        public void _setInf(int newInf, int infStamp)
        {
            if (newInf != this.inf)
            {
                this.inf = newInf;
                this.infStamp = infStamp;
            }
        }

        /**
         * set a new lower bound to the node, and saving it if needed
         * @param newInf the new value of the lower bound
         */
        public void setInf(int newInf)
        {
            if (newInf != this.inf)
            {
                if (this.infStamp <= tree.getEnvironment().getWorldIndex())
                {
                    myTrail.stack(tree,this,INF);
                    this.infStamp = tree.getEnvironment().getWorldIndex();
                }
                this.inf = newInf;
            }
        }

        /**
         * set a new upper bound to the node, without saving it
         * used for trailing purpose
         * @param newSup the new value of the upper bound
         * @param supStamp the new timestamp of the upper bound
         */
        public void _setSup(int newSup, int supStamp)
        {
            if (newSup != this.sup)
            {

                this.sup = newSup;
                this.supStamp = supStamp;

            }
        }

        /**
         * set a new upper bound to the node, and saving it if needed
         * used for trailing purpose
         * @param newSup the new value of the upper bound
         */
        public void setSup(int newSup)
        {
            if (newSup != this.sup)
            {

                if (this.supStamp <= tree.getEnvironment().getWorldIndex())
                {
                    myTrail.stack(tree,this,SUP);
                    this.supStamp = tree.getEnvironment().getWorldIndex();
                }
                this.sup = newSup;
            }
        }

        /**
         * Gets the lower bound of the node
         * @return the lower bound
         */
        public int getInf()
        {
            return inf;
        }

        /**
         * Gets the upper bound of the node
         * @return the upper bound
         */
        public int getSup()
        {
            return sup;
        }


        /**
         * return a String representing the node as an interval
         * @return a new string
         */
        public String toString() {
            return "["+inf+ ',' +sup+ ']';
        }

    }


    /**
     * Computes the size of the tree i.e. the number of value contained in the tree
     * @return integer value of the size
     */
    int getSize();

    /**
     * finds the node which contains the given value
     * @param value the value to be found
     * @return the node which contains the value, null otherwise
     */
    Node find (int value);

    /**
     * Remove a node from the tree
     * @param n the node to be removed;
     */
    void remove(Node n);

    /**
     * Remove a node from the tree, and save the operation if requested
     * @param n the node to be removed
     * @param save true if the removal is to be stacked in the trail
     */
    void remove(Node n, boolean save);

    /**
     * add a new interval [a,b] in the tree
     * @param a the lower bound
     * @param b the upper bound
     */
    void add(int a, int b);

    /**
     * Add a node in the tree
     * @param n the node to be added
     */
    void add(Node n);

    /**
     * Add a node in the tree and save the operation if requested
     * @param n the node to be added
     * @param save true id the addition is to be stacked in the trail
     */
    void add(Node n, boolean save);

    /**
     * Gets the root of the tree
     * @return the root node of the tree
     */
    Node getRoot();

    /**
     * Remove the given value from the tree i.e. updates the tree so that contains(value) would return false;
     * @param value the value to be removed
     * @return true if the value was in the domain, false otherwise
     */
    boolean remove(int value);

    /**
     * Gets the trail associated with a binar tree
     * Used for trailing only
     * @return a trail of StoredBinaryTree
     */
    //StoredBinaryTreeTrail getTrail();

    /**
     * Gets the Environment associated with this tree
     * @return the environment associated with this tree
     */
    EnvironmentTrailing getEnvironment();

    /**
     * Gets the node containing the lowest value in the tree
     * @return first node in the tree
     */
    Node getFirstNode();

    /**
     * Gets the node containing the greatest value in the tree
     * @return last node in the tree
     */
    Node getLastNode();

    /**
     * gets the previous node of the given one
     * @param n the reference node
     * @return the previous node in the lexicographic order
     */
    Node prevNode(Node n);

    /**
     * gets the next node of the given one
     * @param n the reference node
     * @return the next node in the lexicographic order
     */
    Node nextNode(Node n);

    /**
     * gets the node that contains the next integer in the tree
     * @param value the value whose next is to be searched
     * @return the node containing the next value
     */
    Node nextNode(int value);

    /**
     * gets the node that contains the previous integer in the tree
     * @param value the value whose previous is to be searched
     * @return the node containing the previous value
     */
    Node prevNode(int value);

    String toString();

    /**
     * gets an iterator over the value in the tree
     * @return an TreeIterator implementing IntIterator
     */
    DisposableIntIterator getIterator();

    /**
     *  get the tree in dot format
     * @return a string
     */
    String toDotty();


}