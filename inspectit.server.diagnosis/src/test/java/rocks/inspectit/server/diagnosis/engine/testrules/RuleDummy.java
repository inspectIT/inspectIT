package rocks.inspectit.server.diagnosis.engine.testrules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
@Rule(name = "RuleDummy", description = "Test")
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

	public static Method action2Method() {
		return wrap(new Callable<Method>() {
			@Override
			public Method call() throws Exception {
				return RuleDummy.class.getDeclaredMethod("action2");
			}
		});
	}

	public static Method successConditionMethod() {
		return wrap(new Callable<Method>() {
			@Override
			public Method call() throws Exception {
				return RuleDummy.class.getDeclaredMethod("successCondiction");
			}
		});
	}

	public static Method invalidConditionMethod() {
		return wrap(new Callable<Method>() {
			@Override
			public Method call() throws Exception {
				return RuleDummy.class.getDeclaredMethod("invalidCondition");
			}
		});
	}

	public static Method failConditionMethod() {
		return wrap(new Callable<Method>() {
			@Override
			public Method call() throws Exception {
				return RuleDummy.class.getDeclaredMethod("failConidtion");
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

	@Condition(name = "TestFailCondition", hint = "failure")
	public boolean failConidtion() {
		return false;
	}

	@Condition(name = "TestSuccessCondition", hint = "success")
	public boolean successCondiction() {
		return true;
	}

	@Condition(name = "TestInvalidCondition", hint = "invalid")
	public boolean invalidCondition() {
		throw new RuntimeException("TestException");
	}

	@Action(resultTag = "T1")
	public Object action() {
		return "action";
	}

	@Action(resultTag = "T2")
	public Object[] action2() {
		return new String[] { "action1", "action2" };
	}
}
