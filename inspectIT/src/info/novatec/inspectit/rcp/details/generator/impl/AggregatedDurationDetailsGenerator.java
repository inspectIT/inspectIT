package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.details.DetailsCellContent;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Generator that displays the duration info from the {@link TimerData} and it's subclasses.
 * 
 * @author Ivan Senic
 * 
 */
public class AggregatedDurationDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Only for aggregated data.
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof TimerData && defaultData instanceof IAggregatedData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		TimerData timerData = (TimerData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Duration Info", 4);

		// first headings
		table.addContentRow("", null, new DetailsCellContent[] { new DetailsCellContent("Count"), new DetailsCellContent("Avg (ms)"), new DetailsCellContent("Min (ms)"),
				new DetailsCellContent("Max (ms)") });

		// then the total duration
		DetailsCellContent[] total = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatLong(timerData.getCount())),
				new DetailsCellContent(NumberFormatter.formatDouble(timerData.getAverage())), new DetailsCellContent(NumberFormatter.formatDouble(timerData.getMin())),
				new DetailsCellContent(NumberFormatter.formatDouble(timerData.getMax())) };
		table.addContentRow("Total:", null, total);

		if (timerData.isCpuMetricDataAvailable()) {
			// then the total duration
			DetailsCellContent[] cpu = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatLong(timerData.getCount())),
					new DetailsCellContent(NumberFormatter.formatDouble(timerData.getCpuAverage())), new DetailsCellContent(NumberFormatter.formatDouble(timerData.getCpuMin())),
					new DetailsCellContent(NumberFormatter.formatDouble(timerData.getCpuMax())) };
			table.addContentRow("CPU:", null, cpu);
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			// then the exclusive
			DetailsCellContent[] exclusive = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatLong(timerData.getCount())),
					new DetailsCellContent(NumberFormatter.formatDouble(timerData.getExclusiveAverage())), new DetailsCellContent(NumberFormatter.formatDouble(timerData.getExclusiveMin())),
					new DetailsCellContent(NumberFormatter.formatDouble(timerData.getExclusiveMax())) };
			table.addContentRow("Exclusive:", null, exclusive);
		}

		return table;
	}

}
