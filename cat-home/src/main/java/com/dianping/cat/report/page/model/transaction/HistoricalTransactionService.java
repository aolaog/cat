package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.impl.TransactionReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private ReportBucketManager m_bucketManager;

	@Inject
	private TransactionReportService m_reportService;

	public HistoricalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		TransactionReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private TransactionReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp),
		      new Date(timestamp + TimeHelper.ONE_HOUR));
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TransactionAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
