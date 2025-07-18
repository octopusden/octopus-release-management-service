package org.octopusden.octopus.automation.releasemanagement.helper

import org.apache.commons.text.StringEscapeUtils
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.event.EventCartridge
import org.apache.velocity.app.event.ReferenceInsertionEventHandler
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader

import java.io.StringWriter
import org.slf4j.LoggerFactory


class VelocityEngine {
    companion object {
        private val LOG = LoggerFactory.getLogger(VelocityEngine::class.java.`package`.name)
    }

    fun generate(contextMap: Map<String, Any>, templateFile: String, escapeHtml: Boolean = false): String {
        LOG.info("Loading $templateFile")
        val ve = org.apache.velocity.app.VelocityEngine()
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        ve.init()
        val template = ve.getTemplate(templateFile, "UTF-8")
        return getResult(contextMap, template, escapeHtml)
    }

    private fun getEventCartridge(): EventCartridge {
        val eventCartridge = EventCartridge()
        eventCartridge.addReferenceInsertionEventHandler(ReferenceInsertionEventHandler { _, _, value ->
            if (value == null) {
                return@ReferenceInsertionEventHandler null
            }
            StringEscapeUtils.escapeHtml4(value.toString())
        })
        return eventCartridge
    }

    private fun getResult(contextMap: Map<String, Any>, template: Template, escapeHtml: Boolean): String {
        val context = getVelocityContext(contextMap, escapeHtml)
        val writer = StringWriter()
        template.merge(context, writer)
        return writer.toString()
    }

    private fun getVelocityContext(contextMap: Map<String, Any>, escapeHtml: Boolean): VelocityContext {
        val context = VelocityContext()
        if (escapeHtml) {
            context.attachEventCartridge(getEventCartridge())
        }
        for (key in contextMap.keys) {
            context.put(key, contextMap[key])
        }
        return context
    }
}
