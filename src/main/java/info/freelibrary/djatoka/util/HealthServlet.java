
package info.freelibrary.djatoka.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthServlet extends HttpServlet {

    private static final long serialVersionUID = -1456866062313365312L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthServlet.class);

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse)
            throws ServletException, IOException {
        final boolean detailedOutput = aRequest.getParameter("detailed") != null;
        final ServletOutputStream out = aResponse.getOutputStream();
        final Element root = new Element("response");
        final Document response = new Document(root);
        final Element health = new Element("health");
        final Serializer serializer = new Serializer(out);
        final Runtime runtime = Runtime.getRuntime();
        final long freeMemory = runtime.freeMemory();
        final long totalMemory = runtime.totalMemory();
        final long usedMemory = totalMemory - freeMemory;
        final double percentage = (double) usedMemory / totalMemory;
        final String memUsage = String.format("%.2g", percentage);
        int memory;

        serializer.setIndent(2);
        root.appendChild(health);

        memory = (int) (Double.parseDouble(memUsage) * 100);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Memory usage at {}%", memory);
        }

        // These numbers are just a guess... need some real world tests
        // also, add other things like deadlocked threads, etc?
        if (memory < 85) {
            health.appendChild("ok");
        } else {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Memory usage at {}%", memory);
            }

            if (memory > 95) {
                health.appendChild("dying");
            } else {
                health.appendChild("sick");
            }
        }

        if (detailedOutput) {
            root.appendChild(getMemoryStats(memory, freeMemory, totalMemory));
            root.appendChild(getProcessorStats());
            root.appendChild(getThreadStats());
        }

        serializer.write(response);
        out.close();
    }

    private Element getThreadStats() {
        final Element threads = new Element("threads");
        final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        final int threadCount = mxBean.getThreadCount();
        final long[] deadlocked = mxBean.findDeadlockedThreads();
        final long startedCount = mxBean.getTotalStartedThreadCount();
        final int deadCount = deadlocked != null ? deadlocked.length : 0;
        final int peakCount = mxBean.getPeakThreadCount();
        final long totalCount = mxBean.getTotalStartedThreadCount();
        final Element threadCountElem = new Element("threadCount");
        final Element deadlockedCountElem = new Element("deadlockedCount");
        final Element peakCountElem = new Element("peakCount");
        final Element totalCountElem = new Element("totalCount");
        final Element startedCountElem = new Element("totalStartedCount");
        threadCountElem.appendChild(String.valueOf(threadCount));
        deadlockedCountElem.appendChild(String.valueOf(deadCount));
        peakCountElem.appendChild(String.valueOf(peakCount));
        totalCountElem.appendChild(String.valueOf(totalCount));
        startedCountElem.appendChild(String.valueOf(startedCount));

        threads.appendChild(threadCountElem);
        threads.appendChild(deadlockedCountElem);
        threads.appendChild(peakCountElem);
        threads.appendChild(totalCountElem);
        threads.appendChild(startedCountElem);

        return threads;
    }

    private Element getProcessorStats() {
        final Runtime runtime = Runtime.getRuntime();
        final String processors = Integer.toString(runtime.availableProcessors());
        final Element processorsElem = new Element("processors");

        processorsElem.appendChild(processors);
        return processorsElem;
    }

    private Element getMemoryStats(final int aMemPct, final long aFreeMem, final long aTotalMem) {
        final long maxMemory = Runtime.getRuntime().totalMemory();
        final Element freeMemElem = new Element("freeMem");
        final Element totalMemElem = new Element("totalMem");
        final Element maxMemElem = new Element("maxMem");
        final Element memoryElem = new Element("memory");
        final Attribute memPctAtt = new Attribute("pctUsed", String.valueOf(aMemPct));
        final int mb = 1024 * 1024;
        final String freeMB = Long.toString(aFreeMem / mb);
        final String totalMB = Long.toString(aTotalMem / mb);
        final String maxMB = Long.toString(maxMemory / mb);

        freeMemElem.appendChild(Long.toString(aFreeMem));
        freeMemElem.addAttribute(new Attribute("mb", freeMB));
        totalMemElem.appendChild(Long.toString(aTotalMem));
        totalMemElem.addAttribute(new Attribute("mb", totalMB));
        maxMemElem.appendChild(Long.toString(maxMemory));
        maxMemElem.addAttribute(new Attribute("mb", maxMB));

        memoryElem.appendChild(freeMemElem);
        memoryElem.appendChild(totalMemElem);
        memoryElem.appendChild(maxMemElem);
        memoryElem.addAttribute(memPctAtt);

        return memoryElem;
    }
}
