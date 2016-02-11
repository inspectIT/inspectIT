package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.dao.TimerDataDao;
import info.novatec.inspectit.cmr.spring.aop.MethodLog;
import info.novatec.inspectit.communication.data.TimerData;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	@MethodLog
	public List<TimerData> getAggregatedTimerData(TimerData timerData) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@MethodLog
	public List<TimerData> getAggregatedTimerData(TimerData timerData, Date fromDate, Date toDate) {
		List<TimerData> result = timerDataDao.getAggregatedTimerData(timerData, fromDate, toDate);
		return result;
	}

}
