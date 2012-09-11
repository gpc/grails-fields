package grails.plugin.formfields.taglib

import grails.test.mixin.TestFor
import spock.lang.Issue
import grails.plugin.formfields.*

@Issue('https://github.com/robfletcher/grails-fields/issues/45')
@TestFor(FormFieldsTagLib)
class DisplayTagSpec extends AbstractFormFieldsTagLibSpec {

	def mockFormFieldsTemplateService = Mock(FormFieldsTemplateService)

	def setupSpec() {
		configurePropertyAccessorSpringBean()
	}

	def setup() {
		def taglib = applicationContext.getBean(FormFieldsTagLib)

		taglib.formFieldsTemplateService = mockFormFieldsTemplateService
	}

    private void prepareBirthOfDate(String timeZone) {
        def forcedTimeZone = TimeZone.getTimeZone(timeZone)
        TimeZone.setDefault(forcedTimeZone)

        def c = Calendar.instance
        c.setTimeZone(forcedTimeZone)
        c.set(1987, 3, 19, 0, 0, 0)

        personInstance.dateOfBirth = c.time
    }

	void 'renders value using g:fieldValue if no template is present'() {
		expect:
		applyTemplate('<f:display bean="personInstance" property="name"/>', [personInstance: personInstance]) == personInstance.name
	}

	void 'renders boolean values using g:formatBoolean'() {
		given:
		messageSource.addMessage('default.boolean.true', request.locale, 'Yes')

		expect:
		applyTemplate('<f:display bean="personInstance" property="minor"/>', [personInstance: personInstance]) == 'Yes'
	}

    @Issue('https://github.com/robfletcher/grails-fields/issues/99')
	void 'renders date values using g:formatDate'() {
		given:
		messageSource.addMessage('default.date.format', request.locale, 'yyyy-MM-dd HH:mm:ss')

		expect:
		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00/
	}

	void 'renders date values using g:formatDate with dateformat and timezone UTC'() {
		given:
		messageSource.addMessage('default.date.format', request.locale, 'yyyy-MM-dd HH:mm:ss z')
        prepareBirthOfDate("UTC")

   		expect:
   		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00 UTC/

        and:
        TimeZone.setDefault(null)
	}

	void 'renders date values using g:formatDate with dateformat and timezone GMT'() {
		given:
		messageSource.addMessage('default.date.format', request.locale, 'yyyy-MM-dd HH:mm:ss z')
        prepareBirthOfDate("GMT")

   		expect:
   		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00 GMT/

        and:
        TimeZone.setDefault(null)
	}

    @Issue('https://github.com/robfletcher/grails-fields/issues/99')
    void 'renders date values using g:formatDate without default dateformat'() {
   		expect:
   		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00 [A-Z]{3,4}/
   	}

    void 'renders date values using g:formatDate without default dateformat and timezone UTC'() {
        given:
        prepareBirthOfDate("UTC")

   		expect:
   		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00 UTC/

        and:
        TimeZone.setDefault(null)
   	}

    void 'renders date values using g:formatDate without default dateformat and timezone GMT'() {
        given:
        prepareBirthOfDate("GMT")

   		expect:
   		applyTemplate('<f:display bean="personInstance" property="dateOfBirth"/>', [personInstance: personInstance]) ==~ /1987-04-19 00:00:00 GMT/

        and:
        TimeZone.setDefault(null)
   	}

	void 'displays using template if one is present'() {
		given:
		views["/_fields/default/_display.gsp"] = '<dt>${label}</dt><dd>${value}</dd>'

		and:
		mockFormFieldsTemplateService.findTemplate(_, 'display') >> [path: '/_fields/default/display']

		expect:
		applyTemplate('<f:display bean="personInstance" property="name"/>', [personInstance: personInstance]) == '<dt>Name</dt><dd>Bart Simpson</dd>'
	}

	@Issue('https://github.com/robfletcher/grails-fields/issues/88')
	void 'display tag will use body for rendering value'() {
		given:
		views["/_fields/default/_display.gsp"] = '<dt>${label}</dt><dd>${value}</dd>'

		and:
		mockFormFieldsTemplateService.findTemplate(_, 'display') >> [path: '/_fields/default/display']

		expect:
		applyTemplate('<f:display bean="personInstance" property="name">${value.reverse()}</f:display>', [personInstance: personInstance]) == '<dt>Name</dt><dd>nospmiS traB</dd>'
	}

    void 'can nest f:display inside f:with'() {
        expect:
        applyTemplate('<f:with bean="personInstance"><f:display property="name"/></f:with>', [personInstance: personInstance]) == personInstance.name
    }

}
