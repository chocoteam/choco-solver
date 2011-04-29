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

package choco.kernel.memory.trailing;


import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateBinaryTree;
import choco.kernel.memory.trailing.trail.StoredBinaryTreeTrail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 24, 2008
 * Time: 1:27:38 PM
 */
public final class StoredBinaryTree implements IStateBinaryTree {



    private final EnvironmentTrailing env;
    private Node root;
    private boolean addLeft = true;
    private boolean remLeft = true;

    protected final StoredBinaryTreeTrail myTrail;

    public StoredBinaryTree(final EnvironmentTrailing anEnvironment, final int a, final int b)
    {
        this.env = anEnvironment;
        this.myTrail = env.getBinaryTreeTrail();
        this.add(a,b);
    }


    public int getSize() {
        int out = 0;
        final DisposableIntIterator it = this.getIterator();
        while (it.hasNext())
        {
            it.next();
            out++;
        }
        it.dispose();
        return out;
    }

    public Node find (final int value)
    {
        Node current = this.root;
        while (current != null)
        {
            if (current.contains(value)){
                return current;
            }
            if (value < current.inf){
                current = current.leftNode;
            }else{
                current = current.rightNode;
            }
        }
        return current;
    }



    public Node nextNode(final int value)
    {
        Node current = this.root;
        while (current != null)
        {
            if (current.contains(value+1)){
                return current;
            }else if (value+1 < current.inf)
            {
                final Node n = current.leftNode;
                if (n == null){
                    return current;
                }
                current = n;
            }
            else
            {
                final Node n = current.rightNode;
                if (n == null){
                    return this.nextNode(current);
                }
                current = n;


            }


        }
        return null;
    }
    public Node prevNode(final int value)
    {
        Node current = this.root;
        while (current != null)
        {
            if (current.contains(value-1)){
                return current;
            }else if (value-1 < current.inf)
            {
                final Node n = current.leftNode;
                if (n == null){
                    return this.prevNode(current);
                }
                current = n;
            }
            else
            {
                final Node n = current.rightNode;
                if (n == null){
                    return current;
                }
                current = n;


            }


        }
        return null;
    }



    public void add(final Node n)
    {
        add(n,true);
    }

    public void remove(final Node n)
    {
        remove(n,true);
    }


    public void remove(final Node n, final boolean save)
    {
        if (save)
        {
            myTrail.stack(this,n,REM);
        }


        if (n.leftNode == null && n.rightNode == null)
        {
            if (n.father == null){
                this.root = null;
            }else if (n.father.leftNode == n){
                n.father.leftNode = null;
            }else{
                n.father.rightNode = null;
            }
        }
        else if (n.leftNode == null)
        {
            n.rightNode.father = n.father;
            if (n.father == null){
                this.root = n.rightNode;
            }else if (n.father.leftNode == n){
                n.father.leftNode = n.rightNode;
            }else{
                n.father.rightNode = n.rightNode;
            }
        }
        else if (n.rightNode == null)
        {
            n.leftNode.father = n.father;
            if (n.father == null){
                this.root = n.leftNode;
            }else if (n.father.leftNode == n){
                n.father.leftNode = n.leftNode;
            }else {
                n.father.rightNode = n.leftNode;
            }

        }
        else
        {
            Node current;
            final Node curSon;
            final Node curFat;
            if (remLeft)
            {
                current = n.leftNode;
                while (current.rightNode != null){
                    current = current.rightNode;
                }
                curSon = current.leftNode;
                curFat  = current.father;
            }
            else
            {
                current = n.rightNode;
                while (current.leftNode != null){
                    current = current.leftNode;
                }
                curSon = current.rightNode;
                curFat = current.father;
            }


            if (curFat != n)
            {
                current.rightNode = n.rightNode;
                current.leftNode = n.leftNode;
                current.rightNode.father = current;
                current.leftNode.father = current;
                if (remLeft) {
                    curFat.rightNode = curSon;
                }
                else {
                    curFat.leftNode = curSon;
                }
                if (curSon != null){
                    curSon.father = curFat;
                }
            }
            else
            {
                if (remLeft)
                {
                    current.rightNode = n.rightNode;
                    current.rightNode.father = current;
                }
                else
                {
                    current.leftNode = n.leftNode;
                    current.leftNode.father = current;

                }
            }




            current.father = n.father;
            if (current.father == null){
                this.root = current;
            }else if (current.father.leftNode == n)
            {
                current.father.leftNode = current;
            }
            else
            {
                current.father.rightNode = current;
            }

            remLeft ^= true;

        }
    }

    public void add(final int a, final int b) {
        this.add(new Node(this,a,b),false);
    }


    public void add(final Node n, final boolean save)
    {
        if (save)
        {
        	myTrail.stack(this,n,ADD);
        }
        Node current = this.root;
        boolean done = false;
        if (current == null) {
            this.root = n;
            done = true;
        }
        while (!done)
        {
            if (current.inf > n.inf)
            {
                if (current.leftNode == null)
                {
                    current.leftNode = n;
                    n.father = current;
                    done = true;
                }
                else{
                    current = current.leftNode;
                }

            }
            else if (current.inf < n.inf)
            {
                if (current.rightNode == null)
                {
                    current.rightNode = n;
                    n.father = current;
                    done = true;
                }
                else {
                    current = current.rightNode;
                }
            }
            else          {
                LOGGER.error("GROS PB");
                done = true;
            }
        }
    }

    public Node getRoot() {
        return this.root;
    }

    public Node prevNode(final Node n)
    {
        Node cur = n;
        if (cur.leftNode != null) {
            cur = cur.leftNode;
            while (cur.rightNode != null){
                cur = cur.rightNode;
            }
            return cur;
        }
        else if (cur.father == null){
            return null;
        }else if (cur.father.rightNode == cur){
            return cur.father;
        }else
        {
            while (cur.father != null && cur.father.leftNode == cur){
                cur = cur.father;
            }
            return cur.father ;

        }

    }

    public Node nextNode(final Node n)
    {
        Node cur = n;
        if (cur.rightNode != null) {
            cur = cur.rightNode;
            while (cur.leftNode != null){
                cur = cur.leftNode;
            }
            return cur;
        }
        else if (cur.father == null){
            return null;
        }else if (cur.father.leftNode == cur){
            return cur.father;
        }else
        {
            while (cur.father != null && cur.father.rightNode == cur){
                cur = cur.father;
            }
            return cur.father ;

        }

    }


    public boolean remove(final int value)
    {
        final Node container = this.find(value);

        if (container == null){
            return false;

        }else if (container.getSize() == 1)
        {
            this.remove(container,true);
        }

        else if (container.inf == value)
        {
            container.setInf(container.inf+1);
        }
        else if (container.sup == value)
        {
            container.setSup(container.sup-1);
        }
        else
        {
            final Node n2;
            if (addLeft)
            {
                n2 = new Node(this,value+1,container.sup);
                container.setSup(value-1);
            }
            else
            {
                n2 = new Node(this,container.inf,value-1);
                container.setInf(value+1);
            }
            this.add(n2);
            addLeft ^= true;
        }
        return true;
    }


    public EnvironmentTrailing getEnvironment()
    {
        return env;
    }

    public Node getFirstNode()
    {
        Node current = this.root;
        if (current == null){
            return null;
        }
        while (current.leftNode != null){
            current = current.leftNode;
        }
        return current;
    }

    public Node getLastNode()
    {
        Node current = this.root;
        if (current == null){
            return null;
        }
        while (current.rightNode != null){
            current = current.rightNode;
        }
        return current;
    }

    private void rem(final int value)
    {
        remove(value);
    }


    public String toString()
    {
        return toListString();
        /* StringBuffer b = new StringBuffer();
      b.append("[");
      IntIterator it = this.getIterator();
      while (it.hasNext())
          b.append(it.next()).append(",");

      b.deleteCharAt(b.length()-1);
      b.append("]");

      return b.toString(); */
    }

    public List<Node> toList()
    {
        final ArrayList<Node> out = new ArrayList<Node>(32);
        Node current = this.getFirstNode();
        while (current != null)
        {
            out.add(current);
            current = this.nextNode(current);
        }
        return out;
    }

    public String toListString()
    {
        final StringBuilder buf = new StringBuilder("[");
        final List<Node> tmp = this.toList();
        for (final Node iv : tmp){
            buf.append(iv.toString()).append(' ');
        }
        if (!tmp.isEmpty()){
            buf.deleteCharAt(buf.length()-1);
        }
        buf.append(']');
        return buf.toString();
    }

    private TreeIterator _cachedIterator = null;

    public synchronized DisposableIntIterator getIterator()
    {
        final TreeIterator iter = _cachedIterator;
        if (iter != null && iter.isReusable()) {
            iter.init();
            return iter;
        }
        _cachedIterator = new TreeIterator(this);
        return _cachedIterator;
    }

    private static final class TreeIterator extends DisposableIntIterator
    {
        private final StoredBinaryTree tree;
        private int currentValue;
        private Node currentNode;
        private Node lastNode;

        public TreeIterator(final StoredBinaryTree aTree)
        {
            this.tree = aTree;
            this.init();
        }

        @Override
        public void init() {
            super.init();
            currentValue = Integer.MIN_VALUE;
            currentNode = tree.getFirstNode();
            lastNode = tree.getLastNode();
        }

        public boolean hasNext() {
            return (lastNode != null && currentValue < lastNode.sup);
        }

        public int next() {

            if (currentValue == Integer.MIN_VALUE)
            {
                currentValue = currentNode.inf;
            }
            else if (currentValue+1 <= currentNode.sup)
            {
                currentValue++;
            }
            else
            {
                currentNode = tree.nextNode(currentNode);
                currentValue = currentNode.inf;
            }
            return currentValue;
        }

        public void remove() {
            tree.rem(currentValue);
            currentNode = tree.nextNode(currentValue);
            lastNode = tree.getLastNode();

        }
    }

    public String toDotty()
    {
        final StringBuilder s = new StringBuilder("digraph binary_tree_domain {\n");
        s.append(this.toDotty(this.root));
        s.append("\n}");
        return s.toString();
    }



    public String toDotty(final Node n)
    {
        final StringBuilder s = new StringBuilder(32);
        if (n.leftNode != null)
        {
            s.append('\"').append(n).append("\" -> \"").append(n.leftNode).append("\";\n");
            s.append(this.toDotty(n.leftNode));
        }
        if (n.rightNode != null)
        {
            s.append('\"').append(n).append("\" -> \"").append(n.rightNode).append("\";\n");
            s.append(this.toDotty(n.rightNode));
        }


        return s.toString();

    }

    public static void print(final IStateBinaryTree b)
    {

        final String f = "bui.dot";
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(f)));
            bw.write(b.toDotty());
            bw.flush();
            bw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}