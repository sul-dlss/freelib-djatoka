/**
 * Copyright 2006 OCLC Online Computer Library Center Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oclc.oomRef.descriptors;

import info.openurl.oom.descriptors.ByValueMetadataKev;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A <em>Descriptor</em> that specifies properties of an <em>Entity</em>
 * by the combination of: (1) a URI reference to a <em>Metadata
 * Format</em> and (2) a particular instance of metadata about the
 * <em>Entity</em>, expressed according to the indicated <em>Metadata
 * Format</em>.
 * 
 * @author Jeffrey A. Young
 */
public class ByValueMetadataKevImpl implements ByValueMetadataKev {
	private URI val_fmt;
	private Map fieldMap = new HashMap();
	
	/**
	 * Constructs a By-Value Metadata descriptor
	 * 
	 * @param val_fmt A URI reference to a <em>Metadata Format</em>.
	 * @param prefix The KEV key prefix to be extracted from the entrySet
	 * @param entrySet A set of all KEV keys from which a subset
	 * will be extracted according to the specified prefix.
	 */
	public ByValueMetadataKevImpl(URI val_fmt, String prefix, Set entrySet) {
		this.val_fmt = val_fmt;
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String key = (String) entry.getKey();
			if (entry.getKey().toString().startsWith(prefix)) {
				fieldMap.put(key, entry.getValue());
			}
		}
	}

	public ByValueMetadataKevImpl(Set entrySet)
	throws URISyntaxException {
		Iterator iter = entrySet.iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Entry) iter.next();
			String key = (String) entry.getKey();
			if ("genre".equals(key)) {
				String[] values = (String[])entry.getValue();
				if (values.length > 0) {
					this.val_fmt = new URI("info:ofi/fmt:kev:mtx:" + values[0]);
				}
				fieldMap.put("rft.genre", entry.getValue());
				
			} else if ("aulast".equals(key)) {
				fieldMap.put("rft.aulast", entry.getValue());
			} else if ("aufirst".equals(key)) {
				fieldMap.put("rft.aufirst", entry.getValue());
			} else if ("auinit".equals(key)) {
				fieldMap.put("rft.auinit", entry.getValue());
			} else if ("auinit1".equals(key)) {
				fieldMap.put("rft.auinit1", entry.getValue());
			} else if ("auinitm".equals(key)) {
				fieldMap.put("rft.auinitm", entry.getValue());
			} else if ("coden".equals(key)) {
				fieldMap.put("rft.coden", entry.getValue());
			} else if ("issn".equals(key)) {
				fieldMap.put("rft.issn", entry.getValue());
			} else if ("eissn".equals(key)) {
				fieldMap.put("rft.eissn", entry.getValue());
			} else if ("isbn".equals(key)) {
				fieldMap.put("rft.isbn", entry.getValue());
			} else if ("title".equals(key)
					&& "info:ofi/fmt:kev:mtx:journal".equals(this.val_fmt)) {
				fieldMap.put("rft.jtitle", entry.getValue());
			} else if ("title".equals(key)) {
				fieldMap.put("rft.title", entry.getValue());
			} else if ("stitle".equals(key)) {
				fieldMap.put("rft.stitle", entry.getValue());
			} else if ("atitle".equals(key)) {
				fieldMap.put("rft.atitle", entry.getValue());
			} else if ("volume".equals(key)) {
				fieldMap.put("rft.volume", entry.getValue());
			} else if ("part".equals(key)) {
				fieldMap.put("rft.part", entry.getValue());
			} else if ("issue".equals(key)) {
				fieldMap.put("rft.issue", entry.getValue());
			} else if ("spage".equals(key)) {
				fieldMap.put("rft.spage", entry.getValue());
			} else if ("epage".equals(key)) {
				fieldMap.put("rft.epage", entry.getValue());
			} else if ("pages".equals(key)) {
				fieldMap.put("rft.pages", entry.getValue());
			} else if ("artnum".equals(key)) {
				fieldMap.put("rft.artnum", entry.getValue());
			} else if ("sici".equals(key)) {
				fieldMap.put("rft.sici", entry.getValue());
			} else if ("bici".equals(key)) {
				fieldMap.put("rft.bici", entry.getValue());
			} else if ("ssn".equals(key)) {
				fieldMap.put("rft.ssn", entry.getValue());
			} else if ("quarter".equals(key)) {
				fieldMap.put("rft.quarter", entry.getValue());
			} else if ("date".equals(key)) {
				fieldMap.put("rft.date", entry.getValue());
			}
		}
	}

	public URI getValFmt() {
		return val_fmt;
	}
	
	public Map getFieldMap() {
		return fieldMap;
	}
}
