package rocks.inspectit.server.diagnosis.engine.rule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
@Rule(name = "RuleDummy", description = "Test", fireCondition = { "T1,T2" })
public class RuleDummy {

	@TagValue(type = "T1")
	public String tagStringValueField;
	public Tag tagAsTagField;
	public Integer sessionIntVariable;

	public static Method actionMethod() {
		return wrap(new Callable<Method>() {
			@Override
			public Method call() throws Exception {
				return RuleDummy.class.getDeclaredMethod("action");
			}
		});
	}

	public static Field tagStringValueField() {
		return wrap(new Callable<Field>() {
			@Override
			public Field call() throws Exception {
				return RuleDummy.class.getDeclaredField("tagStringValueField");
			}
		});
	}

	public static Field tagAsTagField() {
		return wrap(new Callable<Field>() {
			@Override
			public Field call() throws Exception {
				return RuleDummy.class.getDeclaredField("tagAsTagField");
			}
		});
	}

	public static Field sessionVariableIntField() {
		return wrap(new Callable<Field>() {
			@Override
			public Field call() throws Exception {
				return RuleDummy.class.getDeclaredField("sessionIntVariable");
			}
		});
	}

	private static <T> T wrap(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean failConidtion() {
		return false;
	}

	public boolean successCondiction() {
		return true;
	}

	@Action(resultTag = "T1")
	public Object action() {
		return "action";
	}

}
