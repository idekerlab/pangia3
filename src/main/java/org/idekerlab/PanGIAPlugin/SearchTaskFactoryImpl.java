package org.idekerlab.PanGIAPlugin;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;


public class SearchTaskFactoryImpl extends AbstractTaskFactory
{ //implements TaskFactory {

	private final SearchTask task;

	public SearchTaskFactoryImpl(SearchTask task)
	{
		this.task = task;
	}

	@Override
	public TaskIterator createTaskIterator()
	{
		return new TaskIterator(task);
	}
}
