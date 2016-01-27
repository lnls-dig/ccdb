package org.openepics.discs.conf.ui.trees;

import java.util.ArrayList;
import java.util.List;

import org.openepics.discs.conf.ent.Slot;
import org.openepics.discs.conf.views.SlotView;

/**
 * Adds filtering and buffering support to tree nodes.
 * 
 * @author ilist
 *
 * @param <D> type of data
 */
public class FilteredTreeNode<D> extends TreeNodeWithTree<D> {

	protected List<FilteredTreeNode<D>> bufferedAllChildren = null;
	protected List<FilteredTreeNode<D>> bufferedFilteredChildren = null;
	
	public FilteredTreeNode(D data, BasicTreeNode<D> parent, Tree<D> tree) {
		super(data, parent, tree);
	}

	@Override
	public List<? extends FilteredTreeNode<D>> getFilteredChildren() {
		if (bufferedFilteredChildren == null) {
			getBufferedAllChildren();
			if ("".equals(getTree().getAppliedFilter())) {
				bufferedFilteredChildren = bufferedAllChildren;
			} else {				
				bufferedFilteredChildren = new ArrayList<>();
				for (FilteredTreeNode<D> node : bufferedAllChildren) {
					if (node.isThisNodeAbsolutelyInFilter()) {
						bufferedFilteredChildren.add(node);
					} else if (!node.isLeaf()) {
						bufferedFilteredChildren.add(node);
					} // else remove the leafs
				}
			}
			updateRowKeys();
		}
		return bufferedFilteredChildren;
	}
		
	public List<? extends FilteredTreeNode<D>> getBufferedAllChildren() {
		if (bufferedAllChildren == null) {
			bufferedAllChildren = (List<FilteredTreeNode<D>>)getAllChildren();
		}
		return bufferedAllChildren;
	}
	
    public boolean isThisNodeAbsolutelyInFilter() {
    	SlotView view = (SlotView)getData(); // TREE clean up this code
    	Slot slot = view.getSlot();
    	return !slot.isHostingSlot() || slot.getName().toUpperCase().contains(getTree().getAppliedFilter().toUpperCase());
    }
    
	public void cleanCache() {
		bufferedAllChildren = null;
		bufferedFilteredChildren = null;
	}
	
	public void cleanFilterCache() {
		bufferedFilteredChildren = null;
		if (bufferedAllChildren != null) {
			for (FilteredTreeNode<D> node : bufferedAllChildren) {
				node.cleanFilterCache();
			}
		}
		
	}

	public void refreshCache() {
		List<FilteredTreeNode<D>> oldBuffer = bufferedAllChildren;
		if (oldBuffer == null) return;
		ArrayList<FilteredTreeNode<D>> newBuffer = (ArrayList<FilteredTreeNode<D>>)getAllChildren();
		for (int i = 0; i<newBuffer.size(); i++) {
			Long id = ((SlotView)newBuffer.get(i).getData()).getId();
			for (FilteredTreeNode<D> oldNode : oldBuffer) {
				if (((SlotView)oldNode.getData()).getId().equals(id)) {
					newBuffer.set(i, oldNode);
				}
			}
			((SlotView)newBuffer.get(i).getData()).setFirst(i == 0);
			((SlotView)newBuffer.get(i).getData()).setLast(i == newBuffer.size()-1);
		}
		bufferedAllChildren = newBuffer;
		bufferedFilteredChildren = null;
	}
}