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

import archimulator.model.Experiment;
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentState;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import archimulator.util.ExperimentTableHelper;
import archimulator.util.plot.Table;
import archimulator.web.data.ExperimentPackDataProvider;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.block.BadgeBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.block.LabelBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.BootstrapPagingNavigator;
import net.pickapack.web.util.JavascriptEventConfirmation;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.FileResourceStream;
import org.wicketstuff.annotation.mount.MountPath;

import java.io.*;
import java.text.NumberFormat;
import java.util.List;

/**
 * Experiment packs page.
 *
 * @author Min Cai
 */
@MountPath(value = "/experiment_packs")
public class ExperimentPacksPage extends AuthenticatedBasePage {
    /**
     * Create an experiment pack page.
     *
     * @param parameters the page parameters
     */
    public ExperimentPacksPage(PageParameters parameters) {
        super(parameters);

        setTitle("Experiment Packs - Archimulator");

        IDataProvider<ExperimentPack> dataProviderExperimentPacks = new ExperimentPackDataProvider();

        final DataView<ExperimentPack> rowExperimentPack = new DataView<ExperimentPack>("experimentPack", dataProviderExperimentPacks) {
            {
                setItemsPerPage(10);
            }

            protected void populateItem(Item<ExperimentPack> item) {
                final ExperimentPack experimentPack = item.getModelObject();

                item.setDefaultModel(new CompoundPropertyModel<>(experimentPack));

                final long numTotal = ServiceManager.getExperimentService().getNumExperimentsByParent(experimentPack);
                final long numPending = ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.PENDING);
                final long numReadyToRun = ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.READY_TO_RUN);
                final long numRunning = ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.RUNNING);
                final long numCompleted = ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.COMPLETED);
                final long numAborted = ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.ABORTED);

                item.add(new Label("id"));
                item.add(new Label("title"));
                item.add(new Label("tags", StringUtils.join(experimentPack.getTags(), ",")));
                item.add(new Label("experimentType"));
                item.add(new Label("numMaxInstructions"));
                item.add(new Label("benchmarkTitle"));

                item.add(new WebMarkupContainer("state") {{
                    add(new Label("numTotal", String.format("%d", numTotal)) {{
                        add(new BadgeBehavior());

                        if (numCompleted == numTotal) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-success";
                                }
                            });
                        } else if (numAborted > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-important";
                                }
                            });
                        } else if (numCompleted > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-info";
                                }
                            });
                        }
                    }});
                    add(new Label("numPending", String.format("%d (%s)", numPending,
                            NumberFormat.getPercentInstance().format(numTotal == 0 ? 0 : (double) numPending / numTotal))) {{
                        add(new BadgeBehavior());

                        if (numPending > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-inverse";
                                }
                            });
                        }
                    }});
                    add(new Label("numReadyToRun", String.format("%d (%s)", numReadyToRun,
                            NumberFormat.getPercentInstance().format(numTotal == 0 ? 0 : (double) numReadyToRun / numTotal))) {{
                        add(new BadgeBehavior());

                        if (numReadyToRun > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-inverse";
                                }
                            });
                        }
                    }});
                    add(new Label("numRunning", String.format("%d (%s)", numRunning,
                            NumberFormat.getPercentInstance().format(numTotal == 0 ? 0 : (double) numRunning / numTotal))) {{
                        add(new BadgeBehavior());

                        if (numRunning > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-info";
                                }
                            });
                        }
                    }});
                    add(new Label("numCompleted", String.format("%d (%s)", numCompleted,
                            NumberFormat.getPercentInstance().format(numTotal == 0 ? 0 : (double) numCompleted / numTotal))) {{
                        add(new BadgeBehavior());

                        if (numCompleted > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-success";
                                }
                            });
                        }
                    }});
                    add(new Label("numAborted", String.format("%d (%s)", numAborted,
                            NumberFormat.getPercentInstance().format(numTotal == 0 ? 0 : (double) numAborted / numTotal))) {{
                        add(new BadgeBehavior());

                        if (numAborted > 0) {
                            add(new LabelBehavior() {
                                @Override
                                protected String className() {
                                    return "label-important";
                                }
                            });
                        }
                    }});
                }});

                item.add(new Label("createTimeAsString"));

                item.add(new WebMarkupContainer("operations") {{
                    add(new Link<Void>("start") {
                        {
                            if (!(ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.PENDING) > 0)) {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            } else {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to start?"));
                            }
                        }

                        @Override
                        public void onClick() {
                            ServiceManager.getExperimentService().startExperimentPack(experimentPack);
                        }
                    });

                    add(new Link<Void>("stop") {
                        {
                            if (!(ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.READY_TO_RUN) > 0)) {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            } else {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to stop?"));
                            }
                        }

                        @Override
                        public void onClick() {
                            ServiceManager.getExperimentService().stopExperimentPack(experimentPack);
                        }
                    });

                    add(new Link<Void>("resetCompletedExperiments") {
                        {
                            if (ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.COMPLETED) == 0) {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            } else {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to reset completed experiments?"));
                            }
                        }

                        @Override
                        public void onClick() {
                            ServiceManager.getExperimentService().resetCompletedExperimentsByParent(experimentPack);
                        }
                    });

                    add(new Link<Void>("resetAbortedExperiments") {
                        {
                            if (ServiceManager.getExperimentService().getNumExperimentsByParentAndState(experimentPack, ExperimentState.ABORTED) == 0) {
                                add(new CssClassNameAppender("disabled"));
                                setEnabled(false);
                            } else {
                                add(new JavascriptEventConfirmation("onclick", "Are you sure to reset aborted experiments?"));
                            }
                        }

                        @Override
                        public void onClick() {
                            ServiceManager.getExperimentService().resetAbortedExperimentsByParent(experimentPack);
                        }
                    });

                    add(new BookmarkablePageLink<>("view", ExperimentPackPage.class, new PageParameters() {{
                        set("action", "edit");
                        set("experiment_pack_id", experimentPack.getId());
                        set("back_page_id", ExperimentPacksPage.this.getId());
                    }}));

                    add(new Link<Void>("remove") {
                        {
                            add(new JavascriptEventConfirmation("onclick", "Are you sure to remove?"));
                        }

                        @Override
                        public void onClick() {
                            ServiceManager.getExperimentService().removeExperimentPackById(experimentPack.getId());
                        }
                    });

                    add(new Link<Void>("resultsCsv") {
                        @Override
                        public void onClick() {
                            try {
                                List<Experiment> experiments = ServiceManager.getExperimentService().getStoppedExperimentsByParent(experimentPack);

                                ExperimentTableHelper.sort(experiments);

                                String csv = ServiceManager.getExperimentStatService().tableSummary(experimentPack, experiments).toCsv();

                                File file = File.createTempFile(experimentPack.getTitle(), ".csv");
                                file.deleteOnExit();

                                PrintWriter pw = new PrintWriter(new FileWriter(file));
                                pw.println(csv);
                                pw.close();

                                ResourceStreamResource resource = new ResourceStreamResource(new FileResourceStream(file));
                                getRequestCycle().scheduleRequestHandlerAfterCurrent(new ResourceRequestHandler(resource, new PageParameters()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    add(new Link<Void>("resultsPdf") {
                        @Override
                        public void onClick() {
                            try {
                                List<Experiment> experiments = ServiceManager.getExperimentService().getStoppedExperimentsByParent(experimentPack);

                                ExperimentTableHelper.sort(experiments);

                                Table summary = ServiceManager.getExperimentStatService().tableSummary(experimentPack, experiments).filter(ExperimentStatService.SUMMARY_PDF_FILTER_CRITERIA);

                                File file = File.createTempFile(experimentPack.getTitle(), ".pdf");
                                file.deleteOnExit();

                                Document document = new Document(PageSize.A1);
                                PdfWriter.getInstance(document, new FileOutputStream(file));
                                document.open();

                                document.add(new Paragraph(experimentPack.getTitle()));

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

        add(rowExperimentPack);

        add(new BootstrapPagingNavigator("navigator", rowExperimentPack));
    }
}
