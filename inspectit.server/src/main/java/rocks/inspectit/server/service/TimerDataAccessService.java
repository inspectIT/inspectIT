package rocks.inspectit.server.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rocks.inspectit.server.dao.TimerDataDao;
import rocks.inspectit.server.spring.aop.MethodLog;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.cmr.service.ITimerDataAccessService;

/**
 * Timer data service.
 *
 * @author Ivan Senic
 *
 */
@Service
public class TimerDataAccessService implements ITimerDataAccessService {

	/**
	 * Timer data dao.
	 */
	@Autowired
	private TimerDataDao timerDataDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@MethodLog
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData, fromDate, toDate);
		return result;
	}

}
