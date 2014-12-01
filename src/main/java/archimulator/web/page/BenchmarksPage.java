/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.web.page;

import archimulator.model.Benchmark;
import archimulator.model.Experiment;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import archimulator.util.ExperimentTableHelper;
import archimulator.util.plot.Table;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.wicketstuff.annotation.mount.MountPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Benchmarks page.
 *
 * @author Min Cai
 */
@MountPath(value = "/benchmarks")
public class BenchmarksPage extends AuthenticatedBasePage {
    /**
     * Create a benchmarks page.
     *
     * @param parameters the page parameters
     */
    public BenchmarksPage(PageParameters parameters) {
        super(parameters);

        setTitle("Benchmarks - Archimulator");

        ListView<Benchmark> rowBenchmark = new ListView<Benchmark>("benchmark", ServiceManager.getBenchmarkService().getAllBenchmarks()) {
            protected void populateItem(ListItem<Benchmark> item) {
                final Benchmark benchmark = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(benchmark));

                item.add(new Label("title"));
                item.add(new Label("executable"));
                item.add(new Label("defaultArguments"));
                item.add(new Label("standardIn"));
                item.add(new Label("helperThreadEnabled"));

                item.add(new WebMarkupContainer("operations") {{
                    add(new BookmarkablePageLink<>("view", BenchmarkPage.class, new PageParameters() {{
                        set("action", "view");
                        set("benchmark_title", benchmark.getTitle());
                        set("back_page_id", BenchmarksPage.this.getId());
                    }}));

                    add(new Link<Void>("resultsPdf") {
                        @Override
                        public void onClick() {
                            try {
                                List<Experiment> experiments = ServiceManager.getExperimentService().getStoppedExperimentsByBenchmark(benchmark);

                                ExperimentTableHelper.sort(experiments);

                                Table summary = ServiceManager.getExperimentStatService().tableSummary(null, experiments).filter(ExperimentStatService.SUMMARY_PDF_FILTER_CRITERIA);

                                File file = File.createTempFile(benchmark.getTitle(), ".pdf");
                                file.deleteOnExit();

                                Document document = new Document(PageSize.A1);
                                PdfWriter.getInstance(document, new FileOutputStream(file));
                                document.open();

                                document.add(new Paragraph(benchmark.getTitle()));

                                document.add(new Paragraph(""));

                                PdfPTable table = new PdfPTable(summary.getColumns().size());

                                table.setWidthPercentage(95f);

                                for(String column : summary.getColumns()) {
                                    table.addCell(column);
                                }

                                for(List<String> row : summary.getRows()) {
                                    for(String cell : row) {
                                        table.addCell(cell);
                                    }
                                }

                                document.add(table);
                                document.close();

                                ResourceStreamResource resource = new ResourceStreamResource(new FileResourceStream(file));
                                getRequestCycle().scheduleRequestHandlerAfterCurrent(new ResourceRequestHandler(resource, new PageParameters()));
                            } catch (IOException | DocumentException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }});
            }
        };

        add(rowBenchmark);
    }
}
