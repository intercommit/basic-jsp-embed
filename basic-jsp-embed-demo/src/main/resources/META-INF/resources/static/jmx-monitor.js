$(document).ready(function() {
	var factory = new JmxChartsFactory();
	factory.create([
	                {
	                	chartTitle : 'Memory usage',
	                	chartYAxis : 'Used memory',
	                	chartStacking : 'normal',
	                	chartLegend : 'heap',
	                	name: 'java.lang:type=Memory',
	                	attribute: 'HeapMemoryUsage',
	                	path: 'used'
	                },
	                {
	                	chartLegend : 'nonheap',
	                	name: 'java.lang:type=Memory',
	                	attribute: 'NonHeapMemoryUsage',
	                	path: 'used'
	                }
	                ]);
	factory.create([
	                {
	                	chartTitle : 'Memory committed',
	                	chartYAxis : 'Committed memory',
	                	chartStacking : 'normal',
	                	chartLegend : 'heap',
	                	name: 'java.lang:type=Memory',
	                	attribute: 'HeapMemoryUsage',
	                	path: 'committed'
	                },
	                {
	                	chartLegend : 'nonheap',
	                	name: 'java.lang:type=Memory',
	                	attribute: 'NonHeapMemoryUsage',
	                	path: 'committed'
	                }
	                ]);
	factory.create([
	                {
	                	chartTitle : 'Threads',
	                	chartYAxis : 'Threads',
	                	chartLegend : 'open threads',
	                	name: 'java.lang:type=Threading',
	                	attribute: 'ThreadCount'
	                },
	                {
	                	chartLegend : 'busy threads',
	                	name: 'Tomcat:name="http-bio-8080",type=ThreadPool',
	                	attribute: 'currentThreadsBusy'
	                }
	                ]);
	factory.create([
	                {
	                	name: 'java.lang:type=OperatingSystem',
	                	attribute: 'SystemLoadAverage'
	                }
	                ]);
});

function JmxChartsFactory(keepHistorySec, pollInterval, columnsCount) {
	var jolokia = new Jolokia(jolokiaAgentUrl);
	var series = [];
	var monitoredMbeans = [];
	var chartsCount = 0;

	columnsCount = columnsCount || 3;
	pollInterval = pollInterval || 1000;
	var keepPoints = (keepHistorySec || 600) / (pollInterval / 1000);

	setupPortletsContainer(columnsCount);

	setInterval(function() {
		pollAndUpdateCharts();
	}, pollInterval);

	this.create = function(mbeans) {
		mbeans = $.makeArray(mbeans);
		series = series.concat(createChart(mbeans).series);
		monitoredMbeans = monitoredMbeans.concat(mbeans);
	};

	function pollAndUpdateCharts() {
		var requests = prepareBatchRequest();
		var responses = jolokia.request(requests);
		updateCharts(responses);
	}

	function createNewPortlet(name) {
		return $('#portlet-template')
		.clone(true)
		.appendTo($('.column')[chartsCount++ % columnsCount])
		.removeAttr('id')
		.find('.title').text((name.length > 50? '...' : '') + name.substring(name.length - 50, name.length)).end()
		.find('.portlet-content')[0];
	}

	function setupPortletsContainer() {
		var column = $('.column');
		for(var i = 1; i < columnsCount; ++i){
			column.clone().appendTo(column.parent());
		}
		$(".column").sortable({
			connectWith: ".column"
		});

		$(".portlet-header .ui-icon").click(function() {
			$(this).toggleClass("ui-icon-minusthick").toggleClass("ui-icon-plusthick");
			$(this).parents(".portlet:first").find(".portlet-content").toggle();
		});
		$(".column").disableSelection();
	}

	function prepareBatchRequest() {
		return $.map(monitoredMbeans, function(mbean) {
			return {
				type: "read",
				mbean: mbean.name,
				attribute: mbean.attribute,
				path: mbean.path
			};
		});
	}

	function updateCharts(responses) {
		var curChart = 0;
		$.each(responses, function() {
			var point = {
					x: this.timestamp * 1000,
					y: parseFloat(this.value)
			};
			var curSeries = series[curChart++];
			curSeries.addPoint(point, true, curSeries.data.length >= keepPoints);
		});
	}

	function createChart(mbeans) {
		return new Highcharts.Chart({
			chart: {
				renderTo: createNewPortlet(mbeans[0].chartTitle || mbeans[0].name),
				animation: false,
				defaultSeriesType: 'area',
				shadow: false
			},
			title: { text: null },
			xAxis: { type: 'datetime' },
			yAxis: {
				title: { text: mbeans[0].chartYAxis || mbeans[0].attribute }
			},
			legend: {
				enabled: true,
				borderWidth: 0
			},
			credits: {enabled: false},
			exporting: { enabled: false },
			plotOptions: {
				area: {
					marker: {
						enabled: false
					}
				},
				series: {
					stacking: mbeans[0].chartStacking
				}
			},
			series: $.map(mbeans, function(mbean) {
				return {
					data: [],
					name: mbean.chartLegend || mbean.path || mbean.attribute
				}
			})
		})
	}
}
