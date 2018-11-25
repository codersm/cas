package org.apereo.cas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * The {@link AllCoreTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AdaptiveMultifactorAuthenticationPolicyEventResolverTests.class,
        MultifactorAuthenticationTests.class,
        AdaptiveMultifactorAuthenticationPolicyEventResolverTests.class,
        GroovyScriptMultifactorAuthenticationPolicyEventResolverTests.class,
        TimedMultifactorAuthenticationPolicyEventResolverTests.class
})
public class AllMultifactorAuthenticationWebflowTests {
}
