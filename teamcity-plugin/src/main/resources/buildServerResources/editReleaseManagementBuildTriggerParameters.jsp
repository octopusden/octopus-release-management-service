<%@ include file="/include.jsp" %>
<%@ page import="org.octopusden.octopus.releasemanagementservice.teamcity.plugin.ReleaseManagementBuildTriggerService" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.controllers.BasePropertiesBean" scope="request"/>

<tr class="noBorder">
    <td colspan="2"><em><%=ReleaseManagementBuildTriggerService.DESCRIPTION%></em></td>
</tr>

<tr class="noBorder">
    <td><label for="<%=ReleaseManagementBuildTriggerService.SERVICE_URL%>">Service URL: <l:star/></label></td>
    <td>
        <props:textProperty name="<%=ReleaseManagementBuildTriggerService.SERVICE_URL%>" className="longField"/>
        <span class="smallNote">Base URL of Release Management Service</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.SERVICE_URL%>"></span>
    </td>
</tr>

<tr class="noBorder">
    <td><label for="<%=ReleaseManagementBuildTriggerService.SELECTIONS%>">Selections: <l:star/></label></td>
    <td>
        <props:textProperty name="<%=ReleaseManagementBuildTriggerService.SELECTIONS%>" className="longField" expandable="true"/>
        <span class="smallNote"><u title="- component: component_1
&nbsp;&nbsp;status: RELEASE
- component: component_2
&nbsp;&nbsp;status: RC
&nbsp;&nbsp;minor: 1.1
- component: component_2
&nbsp;&nbsp;status: BUILD
&nbsp;&nbsp;minor: 1.2
- component: component_3
&nbsp;&nbsp;status: BUILD
&nbsp;&nbsp;inReleaseBranch: true">Yaml array</u> of build selections defined by component id, build status, minor version (optionally) and sign of release branch (optionally)</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.SELECTIONS%>"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <td><label for="<%=ReleaseManagementBuildTriggerService.BRANCH%>">Branch: </label></td>
    <td>
        <props:textProperty name="<%=ReleaseManagementBuildTriggerService.BRANCH%>" className="longField"/>
        <span class="smallNote">VCS root target branch. Empty value defines default branch</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.BRANCH%>"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <td><label for="<%=ReleaseManagementBuildTriggerService.POLL_INTERVAL%>">Polling interval: </label></td>
    <td>
        <props:textProperty name="<%=ReleaseManagementBuildTriggerService.POLL_INTERVAL%>" className="longField"/>
        <span class="smallNote">Trigger polling interval in seconds. Empty value defines default TeamCity polling interval</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.POLL_INTERVAL%>"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <td><label for="<%=ReleaseManagementBuildTriggerService.QUIET_PERIOD%>">Quiet period: </label></td>
    <td>
        <props:textProperty name="<%=ReleaseManagementBuildTriggerService.QUIET_PERIOD%>" className="longField"/>
        <span class="smallNote">Quiet period in seconds. It prevents the build from being queued, if the component is released later than (now - quiet_period). Empty value disables the quiet period.</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.QUIET_PERIOD%>"></span>
    </td>
</tr>

<tr class="advancedSetting">
    <td><label for="<%=ReleaseManagementBuildTriggerService.QUEUE_OPTIMIZATION%>">Queue optimization: </label></td>
    <td>
        <props:checkboxProperty name="<%=ReleaseManagementBuildTriggerService.QUEUE_OPTIMIZATION%>"/>
        <span class="smallNote">Enables eviction of previously queued build if it is still in queue</span>
        <span class="error" id="error_<%=ReleaseManagementBuildTriggerService.QUEUE_OPTIMIZATION%>"></span>
    </td>
</tr>