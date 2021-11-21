
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package gui.circuit.drawing;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @param <ElementType>
 * @author sylvester
 */
public final class RTreeNode<ElementType extends PositionedDrawable> {

    public static final class RTreeException extends RuntimeException {

	public RTreeException(Throwable cause) {
	    super(cause);
	}

	public RTreeException(String message, Throwable cause) {
	    super(message, cause);
	}

	public RTreeException(String message) {
	    super(message);
	}

	public RTreeException() {
	}
    }
    private Rectangle bounds;
    private List<RTreeNode<ElementType>> children;
    private ElementType element;
    private RTreeNode<ElementType> parent;
    private int minNodesCount;
    private int maxNodesCount;

    /**
     * Creates a new {@code RTreeNode} using {@code minNodesCount} as minimal
     * and {@code maxNodesCount} as maximal number of childs a node in this
     * graph is allowed to have. Because this creates (an empty) root node (a
     * node without parent) it is also the only way to create an new R-Tree.
     *
     * @param minNodesCount
     * @param maxNodesCount
     */
    public RTreeNode(int minNodesCount, int maxNodesCount) {
	this(minNodesCount, maxNodesCount, new ArrayList<RTreeNode<ElementType>>());
    }

    /**
     * Creates a new {@code RTreeNode} using {@code minNodesCount} as minimal
     * and {@code maxNodesCount} as maximal number of childs a node in this
     * graph is allowed to have. The created nodes children are given by
     * {@code children}.
     * <br/><br/>
     * This is an internal function used to create an
     * non-empty root node and is not be used externally.
     *
     * @param minNodesCount
     * @param maxNodesCount
     * @param children
     */
    private RTreeNode(int minNodesCount, int maxNodesCount, List<RTreeNode<ElementType>> children) {
	this(minNodesCount, maxNodesCount, null, children);
    }

    /**
     * Creates a new {@code RTreeNode} using {@code minNodesCount} as minimal
     * and {@code maxNodesCount} as maximal number of childs a node in this
     * graph is allowed to have. The created nodes children are given by
     * {@code children}, the create nodes parent is given by {@code parent}.
     * <br/><br/>
     * This is an internal convenience function used to create a node already
     * partly inserted in an existing tree. It is not to be used externally.
     *
     * @param minNodesCount
     * @param maxNodesCount
     * @param parent
     * @param children
     */
    private RTreeNode(int minNodesCount, int maxNodesCount, RTreeNode<ElementType> parent, List<RTreeNode<ElementType>> children) {
	this.minNodesCount = minNodesCount;
	this.maxNodesCount = maxNodesCount;
	this.parent = parent;
	this.children = children;
	this.element = null;
	this.setChildrensParent();
	this.calculateBounds();
    }

    /**
     * Creates a new {@code RTreeNode} being a leaf. The node uses
     * {@code minNodesCount} as minimal and {@code maxNodesCount} as maximal
     * number of childs a node in this graph is allowed to have. The created
     * node has no children, but contains the given {@code element} as element.
     * <br/><br/>
     * This is an internal convenience function and is not to be used externally.
     *
     * @param minNodesCount
     * @param maxNodesCount
     * @param element
     */
    private RTreeNode(int minNodesCount, int maxNodesCount, ElementType element) {
	this(minNodesCount, maxNodesCount, null, element);
    }

    /**
     * Creates a new {@code RTreeNode} being a leaf. The node uses
     * {@code minNodesCount} as minimal and {@code maxNodesCount} as maximal
     * number of childs a node in this graph is allowed to have. The created
     * node has no children, but contains the given {@code element} as element.
     * It's parent is given by {@code parent}.
     * <br/><br/>
     * This is an internal convenience function used to create a leaf already
     * partly inserted in an existing tree. It is not to be used externally.
     *
     * @param minNodesCount
     * @param maxNodesCount
     * @param parent
     * @param element
     */
    private RTreeNode(int minNodesCount, int maxNodesCount, RTreeNode<ElementType> parent, ElementType element) {
	this.minNodesCount = minNodesCount;
	this.maxNodesCount = maxNodesCount;
	this.parent = parent;
	this.children = null;
	this.element = element;
	this.setChildrensParent();
	this.calculateBounds();
    }

    /**
     * This adds all {@link T}s given in {@code elements} as
     * children to this tree. The elements are added each to best fitting
     * (indirect) child node of this node or this node itself. Splitting is used
     * for each child node if the new number of children exceed the
     * {@code maxNodesCount} given in the constructor.
     *
     * @param elements
     *
     * @see #split()
     */
    public final void addElements(Collection<ElementType> elements) {
	for (ElementType currentElement : elements)
	    this.addElement(currentElement);
    }

    /**
     * This adds the {@link T} given in {@code element} as
     * child to this tree. The element is added to best fitting (indirect) child
     * node of this node or this node itself. Splitting is used if the new
     * number of children exceed the {@code maxNodesCount} given in the
     * constructor.
     *
     * @param element
     *
     * @see #split()
     */
    public final void addElement(ElementType element) {
	if (this.hasLeaves())
	    this.addChild(new RTreeNode<ElementType>(this.minNodesCount, this.maxNodesCount, this, element));
	else {
	    int minAreaIncrease = Integer.MAX_VALUE;
	    RTreeNode<ElementType> bestChild = null;
	    for (int i = 0; i < this.children.size(); i++) {
		RTreeNode<ElementType> currentChild = this.children.get(i);
		int areaIncrease = getArea(currentChild.getBounds().union(element.getBounds())) - getArea(currentChild.getBounds());
		if (areaIncrease < minAreaIncrease) {
		    minAreaIncrease = areaIncrease;
		    bestChild = currentChild;
		}
	    }
	    bestChild.addElement(element);
	}
    }

    /**
     * This add the given {@code RTreeNode} as direct child to this node. If
     * splitting is required it is used, new bounds are calculated in any case.
     *
     * @param child
     *
     * @see #split()
     */
    private final void addChild(RTreeNode<ElementType> child) {
	this.children.add(child);
	child.setParent(this);
	if (this.children.size() > this.maxNodesCount)
	    this.split();
	else
	    this.calculateBounds();
    }

    /**
     * This removes the {@link T}s given as {@code elements} from
     * the tree emerging from this node. If a element cannot be found in this
     * node or one if it's (indirect) children nothing is done. If neccessary
     * the tree is condensed, i.e. any child that has less than
     * {@code minNodesCount} children is merged with other nodes.
     *
     * @param elements
     *
     * @see #condenseTree()
     */
    public final void removeElements(Collection<ElementType> elements) {
	for (ElementType currentElement : elements)
	    this.removeElement(currentElement);
    }

    /**
     * This removes the {@link T} given as {@code element} from
     * the tree emerging from this node. If the element cannot be found in this
     * node or one if it's (indirect) children nothing is done. If neccessary
     * the tree is condensed, i.e. any child that has less than
     * {@code minNodesCount} children is merged with other nodes.
     *
     * @param element
     *
     * @see #condenseTree()
     */
    public final void removeElement(ElementType element) {
	RTreeNode<ElementType> containingNode = this.findNode(element);
	if (containingNode != null)
	    containingNode.getParent().removeChild(containingNode, false);
    }

    /**
     * This removes the given {@link RTreeNode} from the direct children of this
     * node, if it is found there. If {@code dirty} is set to {@code true} the
     * tree is not condensed afterwards, i.e. this node (or one of it's
     * (indirect) children might have less children as required by
     * {@code minNodesCount} given in the contructor.
     * <br/><br/>
     * Normally {@code dirty} should be set to {@code false}.
     *
     * @param child
     * @param dirty
     *
     * @see #condenseTree()
     */
    private final void removeChild(RTreeNode<ElementType> child, boolean dirty) {
	this.children.remove(child);
	if (!dirty)
	    this.condenseTree();
    }

    /**
     * This function locates the {@link T} given as
     * {@code element} on the subtree of this node. If {@code element} is not
     * located in the subtree {@code null} is returned.
     *
     * @param element
     * @return
     */
    private final RTreeNode<ElementType> findNode(ElementType element) {
	if (this.isLeaf())
	    if (this.element == element)
		return this;
	    else
		return null;
	else {
	    for (RTreeNode<ElementType> child : this.children)
		if (child.getBounds().contains(element.getBounds()))
		    return child.findNode(element);
	    return null;
	}
    }

    /**
     * Returns all components that lie under the given position and are in the
     * subtree opened by this node.
     *
     * @param position The position at which to search.
     * @return The matching components.
     */
    public final List<ElementType> findElements(Point position) {
	LinkedList<RTreeNode<ElementType>> currentNodes = new LinkedList<RTreeNode<ElementType>>();
	List<ElementType> components = new ArrayList<ElementType>();
	currentNodes.add(this);
	RTreeNode<ElementType> currentNode;
	while ((currentNode = currentNodes.pollFirst()) != null)
	    if (currentNode.isLeaf()) {
		if (currentNode.getElement().getDiagram().getTransformedPath().contains(position))
		    components.add(currentNode.getElement());
	    } else
		for (RTreeNode<ElementType> child : currentNode.getChildren())
		    if (child.getBounds().contains(position))
			currentNodes.addLast(child);
	java.util.Collections.sort(components, java.util.Collections.reverseOrder());
	return components;
    }

    /**
     * Returns all components that lie completely within the given frame or just
     * intersect with it and which are in the subtree of this node.
     *
     * @param frame The frame, which is to search.
     * @param mustCompletelyContain If this is set to {@code true} the component
     * must lie completly within the frame, otherwise it must only intersect it.
     * @return The matching components.
     */
    public final List<ElementType> findElements(Rectangle frame, boolean mustCompletelyContain) {
	LinkedList<RTreeNode<ElementType>> currentNodes = new LinkedList<RTreeNode<ElementType>>();
	List<ElementType> components = new ArrayList<ElementType>();
	currentNodes.add(this);
	RTreeNode<ElementType> currentNode;
	while ((currentNode = currentNodes.pollFirst()) != null)
	    if (currentNode.isLeaf())
		components.add(currentNode.getElement());
	    else
		for (RTreeNode<ElementType> child : currentNode.getChildren())
		    if (child.isLeaf()) {
			if (((mustCompletelyContain) && (frame.contains(child.getBounds()))) ||
				((!mustCompletelyContain) && (frame.intersects(child.getBounds()))))
			    currentNodes.addLast(child);
		    } else if (child.getBounds().intersects(frame))
			currentNodes.addLast(child);
	java.util.Collections.sort(components, java.util.Collections.reverseOrder());
	return components;
    }

    /**
     * This returns the children of this node as {@code unmodifiableList}.
     *
     * @return
     *
     * @see java.util.Collections#unmodifiableList(java.util.List)
     */
    public final List<RTreeNode<ElementType>> getChildren() {
	return java.util.Collections.unmodifiableList(children);
    }

    /**
     * This returns the element of this node. If this node is not a leaf
     * {@code null} is returned.
     *
     * @return
     *
     * @see #isLeaf()
     */
    public final ElementType getElement() {
	return this.element;
    }

    /**
     * This returns the bounding rectangle of this node.
     *
     * @return
     */
    public final Rectangle getBounds() {
	return this.bounds;
    }

    /**
     * This returns {@code true} if the current node has no children. In this
     * case the node should have an element, but this is not garanteed.
     * Otherwise {@false} is returned.
     *
     * @return
     *
     * @see #getElement()
     * @see #hasLeaves()
     */
    public final boolean isLeaf() {
	return this.children == null;
    }

    /**
     * This returns {@code true} if the current node has only children, which
     * are leafes. Otherwise {@false} is returned.
     *
     * @return
     *
     * @see #isLeaf()
     */
    public final boolean hasLeaves() {
	if (this.children.size() > 0)
	    return this.children.get(0).isLeaf();
	else
	    return true;
    }

    /**
     * This returns {@code true} if this node is a root node, i.e. has no
     * parent. Otherwise {@code false} is returned.
     *
     * @return
     */
    public final boolean isRoot() {
	return this.parent == null;
    }

    /**
     * This returns the root of the tree this node is located in.
     *
     * @return
     */
    private final RTreeNode<ElementType> getRoot() {
	if (this.isRoot())
	    return this;
	else
	    return this.parent.getRoot();
    }

    /**
     * This function cuts a tree if it's root has only one child, in which case
     * it can be replaced by it's child. This is done until a new root with more
     * than one child is reached.
     * <br/><br/>
     * This function must be invoked on the trees root. It wont have any effect
     * otherwise.
     *
     * @see #isRoot()
     * @see #getRoot()
     */
    private final void cutRoot() {
	while ((this.isRoot()) && (this.children.size() == 1) && (!this.hasLeaves())) {
	    this.children = this.children.get(0).children;
	    this.setChildrensParent();
	}
    }

    /**
     * This returns the parent node of this node.
     *
     * @return
     */
    public final RTreeNode<ElementType> getParent() {
	return parent;
    }

    /**
     * This sets the parent node of this node.
     *
     * @param parent
     */
    private final void setParent(RTreeNode<ElementType> parent) {
	this.parent = parent;
    }

    /**
     * This sets this node as the parent node of all of it's children. This can
     * be used to correct erratic links after moving children from on node to
     * another.
     *
     */
    private final void setChildrensParent() {
	if (this.children != null)
	    for (RTreeNode<ElementType> child : this.children)
		child.setParent(this);
    }

    /**
     * This function adaptes this nodes bounding rectangle to be the smallest
     * rectangle containing the bounding rectablges of all it's children.
     * {@code calculateBounds(true)} is called on this node's parent if this
     * nodes bounding rectangle has changed.
     *
     */
    private final void calculateBounds() {
	this.calculateBounds(true);
    }

    /**
     * This function adaptes this nodes bounding rectangle to be the smallest
     * rectangle containing the bounding rectablges of all it's children.
     * <br/><br/>
     * If {@code propagate} is set to {@code true} {@code calculateBounds(true)}
     * is called on this node's parent if this nodes bounding rectangle has
     * changed.
     *
     * @param propagate
     */
    private final void calculateBounds(boolean propagate) {
	Rectangle newBounds = new Rectangle(0, 0, -1, -1);
	if (this.children != null)
	    for (RTreeNode<ElementType> child : this.children) {
		Rectangle childBounds = child.getBounds();
		newBounds = newBounds.union(childBounds);
	    }
	else if (this.element != null)
	    newBounds = this.element.getBounds();
	if (!newBounds.equals(this.bounds)) {
	    this.bounds = newBounds;
	    if ((!this.isRoot()) && (propagate))
		this.parent.calculateBounds();
	}
    }

    /**
     * This function splits this node in two, using the quadratic splitting
     * alogrithm to determine in which of the two new nodes a child node will
     * be placed.
     */
    private final void split() {
	List<RTreeNode<ElementType>> pool = new ArrayList<RTreeNode<ElementType>>(this.children);
	int elementCount = pool.size();
	List<RTreeNode<ElementType>> group1 = new ArrayList<RTreeNode<ElementType>>(this.minNodesCount);
	List<RTreeNode<ElementType>> group2 = new ArrayList<RTreeNode<ElementType>>(this.minNodesCount);
	Rectangle group1Bounds;
	Rectangle group2Bounds;
	/* Must be set to min value, because maxWaste might be negative, if
	 * elements overlap.*/
	int maxWaste = Integer.MIN_VALUE;
	RTreeNode<ElementType> wastePairFirst = null;
	RTreeNode<ElementType> wastePairSecond = null;

	for (int i = 0; i < elementCount; i++)
	    for (int j = i + 1; j < elementCount; j++) {
		Rectangle iBounds = pool.get(i).getBounds();
		Rectangle jBounds = pool.get(j).getBounds();
		Rectangle joinRegion = iBounds.union(jBounds);
		int waste = getArea(joinRegion) - getArea(iBounds) - getArea(jBounds);
		if (waste > maxWaste) {
		    maxWaste = waste;
		    wastePairFirst = pool.get(i);
		    wastePairSecond = pool.get(j);
		}
	    }
	group1.add(wastePairFirst);
	group1Bounds = new Rectangle(wastePairFirst.getBounds());
	pool.remove(wastePairFirst);
	group2.add(wastePairSecond);
	group2Bounds = new Rectangle(wastePairSecond.getBounds());
	pool.remove(wastePairSecond);

	while ((group1.size() + pool.size() > this.minNodesCount) &&
		(group2.size() + pool.size() > this.minNodesCount) &&
		pool.size() > 0) {
	    /* Must be initilized with less than zero, because the difference
	     * might be exaclty zero if all elements overlap perfectly.*/
	    int maxAreaIncreaseDifference = -1;
	    RTreeNode<ElementType> nextEntry = null;
	    int nextEntryGroup1AreaIncrease = 0;
	    int nextEntryGroup2AreaIncrease = 0;
	    for (int i = 0; i < pool.size(); i++) {
		int group1AreaIncrease = getArea(group1Bounds.union(pool.get(i).getBounds())) - getArea(group1Bounds);
		int group2AreaIncrease = getArea(group2Bounds.union(pool.get(i).getBounds())) - getArea(group2Bounds);
		int areaIncreaseDifference = Math.abs(group1AreaIncrease - group2AreaIncrease);
		if (areaIncreaseDifference > maxAreaIncreaseDifference) {
		    maxAreaIncreaseDifference = areaIncreaseDifference;
		    nextEntry = pool.get(i);
		    nextEntryGroup1AreaIncrease = group1AreaIncrease;
		    nextEntryGroup2AreaIncrease = group2AreaIncrease;
		}
	    }
	    if ((nextEntryGroup1AreaIncrease < nextEntryGroup2AreaIncrease) ||
		    ((nextEntryGroup1AreaIncrease == nextEntryGroup2AreaIncrease) &&
		    (group1.size() < group2.size()))) {
		group1.add(nextEntry);
		group1Bounds = group1Bounds.union(nextEntry.getBounds());
		pool.remove(nextEntry);
	    } else {
		group2.add(nextEntry);
		group2Bounds = group2Bounds.union(nextEntry.getBounds());
		pool.remove(nextEntry);
	    }
	}
	if (!pool.isEmpty())
	    if (group1.size() < group2.size())
		group1.addAll(pool);
	    else
		group2.addAll(pool);
	if (this.isRoot()) {
	    this.children = new ArrayList<RTreeNode<ElementType>>();
	    this.addChild(new RTreeNode<ElementType>(this.minNodesCount, this.maxNodesCount, this, group1));
	    this.addChild(new RTreeNode<ElementType>(this.minNodesCount, this.maxNodesCount, this, group2));
	} else {
	    this.children = group1;
	    this.calculateBounds();
	    this.parent.addChild(new RTreeNode<ElementType>(this.minNodesCount, this.maxNodesCount, this.parent, group2));
	}
    }

    /**
     * This causes the node to check whether it has enough children
     * ({@code minNodesCount}) and to propagate this check upwards to it's
     * ancestors until the root is reached.
     * <br/><br/>
     * If the node has not enough children the node is removed and his remaining
     * children are reinserted in the tree after {@code condenseTree} has
     * reached the root.
     * <br/><br/>
     * This function takes care of correct adaption of the bounding rectangles
     * of the R-Tree.
     */
    public final void condenseTree() {
	this.condenseTree(new LinkedList<ElementType>());
    }

    /**
     * This causes the node to check whether it has enough children
     * ({@code minNodesCount}) and to propagate this check upwards to it's
     * ancestors until the root is reached.
     * <br/><br/>
     * If the node has not enough children the node is removed and his remaining
     * children are reinserted in the tree after {@code condenseTree} has
     * reached the root. To do so, the children are stored in
     * {@code savedElements} which is given to each recursive call of
     * {@code condenseTree} and thus gathers all remaining children.
     * <br/><br/>
     * This function takes care of correct adaption of the bounding rectangles
     * of the R-Tree.
     *
     * @param savedElements
     */
    private final void condenseTree(List<ElementType> savedElements) {
	if ((this.children.size() >= this.minNodesCount) || (this.isRoot())) {
	    this.calculateBounds();
	    this.getRoot().addElements(savedElements);
	    this.getRoot().cutRoot();
	} else {
	    for (RTreeNode<ElementType> child : this.children)
		savedElements.add(child.element);
	    this.parent.removeChild(this, true);
	    this.parent.condenseTree(savedElements);
	}
    }

    /**
     * This helper function returns the area covered by a given rectangle.
     *
     * @param rectangle
     * @return
     */
    private final int getArea(Rectangle rectangle) {
	if ((rectangle.width < 0) || (rectangle.height < 0))
	    return 0;
	else
	    return rectangle.width * rectangle.height;

    }
}
