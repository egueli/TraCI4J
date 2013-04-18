<?xml version='1.0' encoding="UTF-8"?>
<!-- Copyright (C) 2013 ApPeAL Group, Politecnico di Torino This file is 
	part of TraCI4J. TraCI4J is free software: you can redistribute it and/or 
	modify it under the terms of the GNU General Public License as published 
	by the Free Software Foundation, either version 3 of the License, or (at 
	your option) any later version. TraCI4J is distributed in the hope that it 
	will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
	of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
	Public License for more details. You should have received a copy of the GNU 
	General Public License along with TraCI4J. If not, see <http://www.gnu.org/licenses/>. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text"/>

<xsl:param name="queries-file-name" />

<xsl:variable name="queries" select="document($queries-file-name)" />

<xsl:template match="/traciClass">

/*   
    Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

    This file is part of TraCI4J.

    TraCI4J is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TraCI4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TraCI4J.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
*/

/*
THIS FILE IS GENERATED AUTOMATICALLY. DO NOT EDIT: CHANGES WILL BE OVERWRITTEN.
*/

package it.polito.appeal.traci;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class <xsl:value-of select="name"/> 
extends TraciObject&lt;<xsl:value-of select="name"/>.Variable&gt;
implements StepAdvanceListener
{

	public static enum Variable {
		<xsl:for-each select="readQueries/readQuery">
		<xsl:value-of select="enum"/>,
		</xsl:for-each>
	}
	
	<xsl:for-each select="changeStateQueries/changeStateQuery">
	private final <xsl:value-of select="query"/> csqvar_<xsl:value-of select="name"/>;
	</xsl:for-each>
	
	<xsl:value-of select="name"/> (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		<xsl:for-each select="repos/repo">
			, Repository&lt;<xsl:apply-templates/>&gt; repo<xsl:apply-templates/>
		</xsl:for-each>
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		<xsl:for-each select="readQueries/readQuery">
		<xsl:variable name="query-class" select="query" />
		<xsl:variable name="query-data" select="$queries/queries/query[class=$query-class]" />
		addReadQuery(Variable.<xsl:value-of select="enum"/>, 
				new <xsl:value-of select="query"/> (dis, dos, 
				<xsl:value-of select="../../command"/>, 
				id, 
				<xsl:value-of select="const"/>
				<xsl:for-each select="$query-data/usedRepos/repo">
				, repo<xsl:apply-templates/>
				</xsl:for-each>
				
				));
		</xsl:for-each>

		/*
		 * initialization of change state queries
		 */
		<xsl:for-each select="changeStateQueries/changeStateQuery">
		csqvar_<xsl:value-of select="name"/> = new <xsl:value-of select="query"/>(dis, dos, id
		<xsl:if test="const">, <xsl:value-of select="../../changeStateCommand"/>, <xsl:value-of select="const"/></xsl:if>)
		<xsl:if test="affects">{
			@Override
			void pickResponses(java.util.Iterator&lt;it.polito.appeal.traci.protocol.ResponseContainer&gt; responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				<xsl:for-each select="affects/affect">
				query<xsl:apply-templates/>().setObsolete();
				</xsl:for-each>
			}
		}</xsl:if>;
		</xsl:for-each>
	
	}
	
	<!-- CACHE CLEANER -->
	
	@Override
	public void nextStep(double step) {
		<xsl:for-each select="readQueries/readQuery[dynamic='true']">
		getReadQuery(Variable.<xsl:value-of select="enum"/>).setObsolete();
		</xsl:for-each>
	}
	
	
	<!-- QUERY GETTERS -->
	
	<xsl:for-each select="readQueries/readQuery">
	<xsl:choose>
		<xsl:when test="returnType!=''">
	public ReadObjectVarQuery&lt;<xsl:value-of select="returnType"/>&gt; query<xsl:value-of select="name" />() {
		</xsl:when>
		<xsl:otherwise>
	public <xsl:value-of select="query"/> query<xsl:value-of select="name" />() {
		</xsl:otherwise>
	</xsl:choose>  
		return (<xsl:value-of select="query"/>) getReadQuery(Variable.<xsl:value-of select="enum"/>);
	}
	
	</xsl:for-each>
	
	<!--  QUERY SETTERS -->
	
	<xsl:for-each select="changeStateQueries/changeStateQuery">
	public <xsl:value-of select="query" /> query<xsl:value-of select="name"/>() {
		return csqvar_<xsl:value-of select="name"/>;
	}
	</xsl:for-each>
}

</xsl:template>
 
</xsl:stylesheet>