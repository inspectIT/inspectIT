package info.novatec.inspectit.rcp;

/**
 * Defines all the images for the InspectIT UI. Note that all images are automatically added to the
 * registry on the inspectIT start up.
 *
 * Please note that this is NOT all the images available. There are so many additional images that
 * you can find at
 * https://inspectit-performance.atlassian.net/wiki/display/DEV/Icons+and+banners?preview=%2F5019224
 * %2F9109584%2Ffugue-icons-3.5.5.zip
 *
 * @author Ivan Senic
 * @author Stefan Siegl
 */
public interface InspectITImages {

	// NOCHKALL: We will not javadoc comment each image.

	// images created by us
	String IMG_SERVER_ONLINE = InspectITConstants.ICON_PATH_SELFMADE + "server_online.png";
	String IMG_SERVER_OFFLINE = InspectITConstants.ICON_PATH_SELFMADE + "server_offline.png";
	String IMG_SERVER_ONLINE_SMALL = InspectITConstants.ICON_PATH_SELFMADE + "server_online_16x16.png";
	String IMG_SERVER_OFFLINE_SMALL = InspectITConstants.ICON_PATH_SELFMADE + "server_offline_16x16.png";
	String IMG_SERVER_REFRESH = InspectITConstants.ICON_PATH_SELFMADE + "server_refresh.png";
	String IMG_SERVER_REFRESH_SMALL = InspectITConstants.ICON_PATH_SELFMADE + "server_refresh_16x16.png";
	String IMG_SERVER_ADD = InspectITConstants.ICON_PATH_SELFMADE + "server_add.png";
	String IMG_RETURN = InspectITConstants.ICON_PATH_SELFMADE + "return.png";
	String IMG_PARAMETER = InspectITConstants.ICON_PATH_SELFMADE + "parameter.png";
	String IMG_FIELD = InspectITConstants.ICON_PATH_SELFMADE + "field.png";

	// images from Eclipse, license EPL 1.0
	String IMG_ACTIVITY = InspectITConstants.ICON_PATH_ECLIPSE + "debugtt_obj.gif";
	String IMG_ADD = InspectITConstants.ICON_PATH_ECLIPSE + "add_obj.gif";
	String IMG_ALERT = InspectITConstants.ICON_PATH_ECLIPSE + "alert_obj.gif";
	String IMG_ANNOTATION = InspectITConstants.ICON_PATH_ECLIPSE + "annotation_obj.gif";
	String IMG_CALL_HIERARCHY = InspectITConstants.ICON_PATH_ECLIPSE + "call_hierarchy.gif";
	String IMG_CHECKMARK = InspectITConstants.ICON_PATH_ECLIPSE + "complete_status.gif";
	String IMG_CLASS = InspectITConstants.ICON_PATH_ECLIPSE + "class_obj.gif";
	String IMG_CLASS_OVERVIEW = InspectITConstants.ICON_PATH_ECLIPSE + "class_obj.gif";
	String IMG_CLOSE = InspectITConstants.ICON_PATH_ECLIPSE + "remove_co.gif";
	String IMG_COLLAPSE = InspectITConstants.ICON_PATH_ECLIPSE + "collapseall.gif";
	String IMG_COMPILATION_OVERVIEW = InspectITConstants.ICON_PATH_ECLIPSE + "workset.gif";
	String IMG_CONFIGURATION = InspectITConstants.ICON_PATH_ECLIPSE + "config_obj.gif";
	String IMG_COPY = InspectITConstants.ICON_PATH_ECLIPSE + "copy_edit.gif";
	String IMG_DELETE = InspectITConstants.ICON_PATH_ECLIPSE + "delete_obj.gif";
	String IMG_DISABLED = InspectITConstants.ICON_PATH_ECLIPSE + "disabled_co.gif";
	String IMG_EXCEPTION_SENSOR = InspectITConstants.ICON_PATH_ECLIPSE + "exceptiontracer.gif";
	String IMG_EXCEPTION_TREE = InspectITConstants.ICON_PATH_ECLIPSE + "exceptiontree.gif";
	String IMG_EXPORT = InspectITConstants.ICON_PATH_ECLIPSE + "export.gif";
	String IMG_FILTER = InspectITConstants.ICON_PATH_ECLIPSE + "filter_ps.gif";
	String IMG_FOLDER = InspectITConstants.ICON_PATH_ECLIPSE + "prj_obj.gif";
	String IMG_FONT = InspectITConstants.ICON_PATH_ECLIPSE + "font.gif";
	String IMG_HELP = InspectITConstants.ICON_PATH_ECLIPSE + "help.gif";
	String IMG_HTTP = InspectITConstants.ICON_PATH_ECLIPSE + "discovery.gif";
	String IMG_HTTP_AGGREGATION_REQUESTMESSAGE = InspectITConstants.ICON_PATH_ECLIPSE + "showcat_co.gif";
	String IMG_HTTP_TAGGED = InspectITConstants.ICON_PATH_ECLIPSE + "gel_sc_obj.gif";
	String IMG_HOME = InspectITConstants.ICON_PATH_ECLIPSE + "home_nav.gif";
	String IMG_IMPORT = InspectITConstants.ICON_PATH_ECLIPSE + "import.gif";
	String IMG_INFORMATION = InspectITConstants.ICON_PATH_ECLIPSE + "info_obj.gif";
	String IMG_INTERFACE = InspectITConstants.ICON_PATH_ECLIPSE + "int_obj.gif";
	String IMG_ITEM_NA_GREY = InspectITConstants.ICON_PATH_ECLIPSE + "remove_exc.gif";
	String IMG_LIVE_MODE = InspectITConstants.ICON_PATH_ECLIPSE + "start_task.gif";
	String IMG_METHOD_PUBLIC = InspectITConstants.ICON_PATH_ECLIPSE + "methpub_obj.gif";
	String IMG_METHOD_PROTECTED = InspectITConstants.ICON_PATH_ECLIPSE + "methpro_obj.gif";
	String IMG_METHOD_DEFAULT = InspectITConstants.ICON_PATH_ECLIPSE + "methdef_obj.gif";
	String IMG_METHOD_PRIVATE = InspectITConstants.ICON_PATH_ECLIPSE + "methpri_obj.gif";
	String IMG_NEXT = InspectITConstants.ICON_PATH_ECLIPSE + "next_nav.gif";
	String IMG_OVERLAY_UP = InspectITConstants.ICON_PATH_ECLIPSE + "over_co.gif";
	String IMG_PACKAGE = InspectITConstants.ICON_PATH_ECLIPSE + "package_obj.gif";
	String IMG_PREVIOUS = InspectITConstants.ICON_PATH_ECLIPSE + "prev_nav.gif";
	String IMG_PRIORITY = InspectITConstants.ICON_PATH_ECLIPSE + "ihigh_obj.gif";
	String IMG_PROPERTIES = InspectITConstants.ICON_PATH_ECLIPSE + "properties.gif";
	String IMG_PROGRESS_VIEW = InspectITConstants.ICON_PATH_ECLIPSE + "pview.gif";
	String IMG_READ = InspectITConstants.ICON_PATH_ECLIPSE + "read_obj.gif";
	String IMG_REFRESH = InspectITConstants.ICON_PATH_ECLIPSE + "refresh.gif";
	String IMG_REMOVE = InspectITConstants.ICON_PATH_ECLIPSE + "remove_correction.gif";
	String IMG_RESTART = InspectITConstants.ICON_PATH_ECLIPSE + "term_restart.gif";
	String IMG_SEARCH = InspectITConstants.ICON_PATH_ECLIPSE + "insp_sbook.gif";
	String IMG_SHOW_ALL = InspectITConstants.ICON_PATH_ECLIPSE + "all_instances.gif";
	String IMG_STACKTRACE = InspectITConstants.ICON_PATH_ECLIPSE + "stacktrace.gif";
	String IMG_TERMINATE = InspectITConstants.ICON_PATH_ECLIPSE + "terminate_co.gif";
	String IMG_TEST_MAPPINGS = InspectITConstants.ICON_PATH_ECLIPSE + "test.gif";
	String IMG_THREADS_OVERVIEW = InspectITConstants.ICON_PATH_ECLIPSE + "debugt_obj.gif";
	String IMG_TIMESTAMP = InspectITConstants.ICON_PATH_ECLIPSE + "dates.gif";
	String IMG_TRASH = InspectITConstants.ICON_PATH_ECLIPSE + "trash.gif";
	String IMG_WARNING = InspectITConstants.ICON_PATH_ECLIPSE + "warning_obj.gif";
	String IMG_WINDOW = InspectITConstants.ICON_PATH_ECLIPSE + "defaultview_misc.gif";

	// wizard banners, all from Eclipse, license EPL 1.0
	String IMG_WIZBAN_ADD = InspectITConstants.WIZBAN_ICON_PATH + "add_wiz.png";
	String IMG_WIZBAN_DOWNLOAD = InspectITConstants.WIZBAN_ICON_PATH + "download_wiz.png";
	String IMG_WIZBAN_EDIT = InspectITConstants.WIZBAN_ICON_PATH + "edit_wiz.png";
	String IMG_WIZBAN_ERROR = InspectITConstants.WIZBAN_ICON_PATH + "error_wiz.png";
	String IMG_WIZBAN_EXPORT = InspectITConstants.WIZBAN_ICON_PATH + "export_wiz.png";
	String IMG_WIZBAN_IMPORT = InspectITConstants.WIZBAN_ICON_PATH + "import_wiz.png";
	String IMG_WIZBAN_LABEL = InspectITConstants.WIZBAN_ICON_PATH + "label_wiz.png";
	String IMG_WIZBAN_RECORD = InspectITConstants.WIZBAN_ICON_PATH + "record_wiz.png";
	String IMG_WIZBAN_SERVER = InspectITConstants.WIZBAN_ICON_PATH + "server_wiz.png";
	String IMG_WIZBAN_STORAGE = InspectITConstants.WIZBAN_ICON_PATH + "storage_wiz.png";
	String IMG_WIZBAN_UPLOAD = InspectITConstants.WIZBAN_ICON_PATH + "upload_wiz.png";

	// images originally from Eclipse we modified (resized, added smth, etc), license EPL 1.0
	String IMG_AGENT = InspectITConstants.ICON_PATH_ECLIPSE + "agent.gif";
	String IMG_AGENT_ACTIVE = InspectITConstants.ICON_PATH_ECLIPSE + "agent_active.gif";
	String IMG_AGENT_NOT_ACTIVE = InspectITConstants.ICON_PATH_ECLIPSE + "agent_na.gif";
	String IMG_AGENT_NOT_SENDING = InspectITConstants.ICON_PATH_ECLIPSE + "agent_not_sending.gif";
	String IMG_AGENT_NO_KEEPALIVE = InspectITConstants.ICON_PATH_ECLIPSE + "agent_no_keepalive.png";
	String IMG_BUFFER_CLEAR = InspectITConstants.ICON_PATH_ECLIPSE + "buffer_clear.gif";
	String IMG_BUFFER_COPY = InspectITConstants.ICON_PATH_ECLIPSE + "buffer_copy.gif";
	String IMG_BUSINESS = InspectITConstants.ICON_PATH_ECLIPSE + "business.gif";
	String IMG_CALENDAR = InspectITConstants.ICON_PATH_ECLIPSE + "calendar.gif";
	String IMG_CATALOG = InspectITConstants.ICON_PATH_ECLIPSE + "catalog.gif";
	String IMG_CHART_BAR = InspectITConstants.ICON_PATH_ECLIPSE + "graph_bar.gif";
	String IMG_CHART_PIE = InspectITConstants.ICON_PATH_ECLIPSE + "graph_pie.gif";
	String IMG_CLASS_EXCLUDE = InspectITConstants.ICON_PATH_ECLIPSE + "class_exclude.gif";
	String IMG_EDIT = InspectITConstants.ICON_PATH_ECLIPSE + "edit.gif";
	String IMG_FLAG = InspectITConstants.ICON_PATH_ECLIPSE + "flag.gif";
	String IMG_HTTP_URL = InspectITConstants.ICON_PATH_ECLIPSE + "url.gif";
	String IMG_LABEL = InspectITConstants.ICON_PATH_ECLIPSE + "label.gif";
	String IMG_LABEL_ADD = InspectITConstants.ICON_PATH_ECLIPSE + "label_add.gif";
	String IMG_LABEL_DELETE = InspectITConstants.ICON_PATH_ECLIPSE + "label_delete.gif";
	String IMG_LOCATE_IN_HIERARCHY = InspectITConstants.ICON_PATH_ECLIPSE + "locate_in_hierarchy.gif";
	String IMG_LOG = InspectITConstants.ICON_PATH_ECLIPSE + "log.gif";
	String IMG_METHOD = InspectITConstants.ICON_PATH_ECLIPSE + "method.gif";
	String IMG_MESSAGE = InspectITConstants.ICON_PATH_ECLIPSE + "message.gif";
	String IMG_NUMBER = InspectITConstants.ICON_PATH_ECLIPSE + "number.gif";
	String IMG_OVERLAY_ERROR = InspectITConstants.ICON_PATH_ECLIPSE + "overlay_error.gif";
	String IMG_OVERLAY_PRIORITY = InspectITConstants.ICON_PATH_ECLIPSE + "overlay_priority.gif";
	String IMG_PREFERENCES = InspectITConstants.ICON_PATH_ECLIPSE + "preferences.gif";
	String IMG_RECORD = InspectITConstants.ICON_PATH_ECLIPSE + "record.gif";
	String IMG_RECORD_GRAY = InspectITConstants.ICON_PATH_ECLIPSE + "record_gray.gif";
	String IMG_RECORD_SCHEDULED = InspectITConstants.ICON_PATH_ECLIPSE + "record_schedule.gif";
	String IMG_RECORD_STOP = InspectITConstants.ICON_PATH_ECLIPSE + "record_term.gif";
	String IMG_OPTIONS = InspectITConstants.ICON_PATH_ECLIPSE + "options.gif";
	String IMG_SERVER_INSTACE = InspectITConstants.ICON_PATH_ECLIPSE + "server_instance.gif";
	String IMG_STORAGE = InspectITConstants.ICON_PATH_ECLIPSE + "storage.gif";
	String IMG_STORAGE_AVAILABLE = InspectITConstants.ICON_PATH_ECLIPSE + "storage_available.gif";
	String IMG_STORAGE_CLOSED = InspectITConstants.ICON_PATH_ECLIPSE + "storage_readable.gif";
	String IMG_STORAGE_DOWNLOADED = InspectITConstants.ICON_PATH_ECLIPSE + "storage_download.gif";
	String IMG_STORAGE_FINALIZE = InspectITConstants.ICON_PATH_ECLIPSE + "storage_finalize.gif";
	String IMG_STORAGE_NOT_AVAILABLE = InspectITConstants.ICON_PATH_ECLIPSE + "storage_na.gif";
	String IMG_STORAGE_OPENED = InspectITConstants.ICON_PATH_ECLIPSE + "storage_writable.gif";
	String IMG_STORAGE_OVERLAY = InspectITConstants.ICON_PATH_ECLIPSE + "storage_overlay.gif";
	String IMG_STORAGE_RECORDING = InspectITConstants.ICON_PATH_ECLIPSE + "storage_recording.gif";
	String IMG_STORAGE_UPLOAD = InspectITConstants.ICON_PATH_ECLIPSE + "storage_upload.gif";
	String IMG_SUPERCLASS = InspectITConstants.ICON_PATH_ECLIPSE + "class_hierarchy.gif";
	String IMG_TIME = InspectITConstants.ICON_PATH_ECLIPSE + "time.gif";
	String IMG_TIME_DELTA = InspectITConstants.ICON_PATH_ECLIPSE + "time_delta.gif";
	String IMG_TIMEFRAME = InspectITConstants.ICON_PATH_ECLIPSE + "timeframe.gif";
	String IMG_TIMER = InspectITConstants.ICON_PATH_ECLIPSE + "method_time.gif";
	String IMG_TOOL = InspectITConstants.ICON_PATH_ECLIPSE + "build.gif";
	String IMG_TRANSFORM = InspectITConstants.ICON_PATH_ECLIPSE + "transform.gif";
	String IMG_USER = InspectITConstants.ICON_PATH_ECLIPSE + "user.gif";

	// Fugue set - license Creative Commons v3.0
	String IMG_ADDRESSBOOK = InspectITConstants.ICON_PATH_FUGUE + "address-book.png";
	String IMG_ADDRESSBOOK_BLUE = InspectITConstants.ICON_PATH_FUGUE + "address-book-blue.png";
	String IMG_ADDRESSBOOK_PLUS = InspectITConstants.ICON_PATH_FUGUE + "address-book-plus.png";
	String IMG_BLOCK = InspectITConstants.ICON_PATH_FUGUE + "block.png";
	String IMG_COMPASS = InspectITConstants.ICON_PATH_FUGUE + "compass.png";
	String IMG_CPU_OVERVIEW = InspectITConstants.ICON_PATH_FUGUE + "processor.png";
	String IMG_DATABASE = InspectITConstants.ICON_PATH_FUGUE + "database-sql.png";
	String IMG_INSTRUMENTATION_BROWSER = InspectITConstants.ICON_PATH_FUGUE + "blue-document-tree.png";
	String IMG_INSTRUMENTATION_BROWSER_INACTIVE = InspectITConstants.ICON_PATH_FUGUE + "document-tree.png";
	String IMG_INVOCATION = InspectITConstants.ICON_PATH_FUGUE + "arrow-switch.png";
	String IMG_MEMORY_OVERVIEW = InspectITConstants.ICON_PATH_FUGUE + "memory.png";
	String IMG_SYSTEM_OVERVIEW = InspectITConstants.ICON_PATH_FUGUE + "system-monitor.png";
	String IMG_VM_SUMMARY = InspectITConstants.ICON_PATH_FUGUE + "resource-monitor.png";
	String IMG_LOGGING_LEVEL = InspectITConstants.ICON_PATH_FUGUE + "traffic-light-single.png";
	String IMG_BEAN = InspectITConstants.ICON_PATH_FUGUE + "bean.png";
	String IMG_BLUE_DOCUMENT_TABLE = InspectITConstants.ICON_PATH_FUGUE + "blue-document-table.png";
	String IMG_BOOK = InspectITConstants.ICON_PATH_FUGUE + "book.png";
	String IMG_DUMMY = InspectITConstants.ICON_PATH_FUGUE + "dummy-happy.png";
	String IMG_COUNTER = InspectITConstants.ICON_PATH_FUGUE + "counter.png";

	// labels just pointing to existing ones
	String IMG_ASSIGNEE_LABEL_ICON = IMG_USER;
	String IMG_DATE_LABEL_ICON = IMG_CALENDAR;
	String IMG_MOUNTEDBY_LABEL_ICON = IMG_READ;
	String IMG_RATING_LABEL_ICON = IMG_PRIORITY;
	String IMG_STATUS_LABEL_ICON = IMG_ALERT;
	String IMG_USECASE_LABEL_ICON = IMG_BUSINESS;
	String IMG_USER_LABEL_ICON = IMG_DISABLED;

}