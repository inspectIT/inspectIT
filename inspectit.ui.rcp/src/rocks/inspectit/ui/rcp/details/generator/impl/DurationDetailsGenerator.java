package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
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
public class DurationDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Display for non-aggregated {@link TimerData} or {@link InvocationSequenceData}.
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof TimerData && !(defaultData instanceof IAggregatedData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		TimerData timerData = (TimerData) defaultData;

		DetailsTable table = new DetailsTable(parent, toolkit, "Duration Info", 1);

		// then the total duration
		DetailsCellContent[] total = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatDouble(timerData.getDuration())) };
		table.addContentRow("Total (ms):", null, total);

		if (timerData.isCpuMetricDataAvailable()) {
			// then the total duration
			DetailsCellContent[] cpu = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatDouble(timerData.getCpuDuration())) };
			table.addContentRow("CPU (ms):", null, cpu);
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			// then the exclusive
			DetailsCellContent[] exclusive = new DetailsCellContent[] { new DetailsCellContent(NumberFormatter.formatDouble(timerData.getExclusiveDuration())) };
			table.addContentRow("Exclusive (ms):", null, exclusive);
		}

		return table;
	}

}
