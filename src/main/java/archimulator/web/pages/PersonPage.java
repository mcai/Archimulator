package archimulator.web.pages;

import archimulator.web.pages.listeditor.ListEditor;
import archimulator.web.pages.listeditor.ListItem;
import archimulator.web.pages.listeditor.RemoveButton;
import archimulator.web.pages.wicketinaction.Person;
import archimulator.web.pages.wicketinaction.Phone;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.Arrays;
import java.util.List;

@MountPath(value = "/", alt = "/person")
public class PersonPage extends BasePage {
    private Person person = new Person();

    public PersonPage(final PageParameters parameters) {
        super(PageType.EXPERIMENT, parameters);

        final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback") {{
            setOutputMarkupId(true);
        }};
        add(feedbackPanel);

        final Form form = new Form("form") {
            {
                setOutputMarkupId(true);
            }
        };
        add(form);

        form.add(new TextField<String>("name", new PropertyModel<String>(PersonPage.this, "person.name")));

        final AjaxFallbackButton buttonSubmit = new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                info(person.toString());

                if(target != null) {
                    target.add(feedbackPanel);
                }
            }
        };
        form.add(buttonSubmit);

        final ListEditor<Phone> editor = new ListEditor<Phone>("phones", new PropertyModel<List<Phone>>(PersonPage.this, "person.phones")) {
            @Override
            protected void onPopulateItem(final ListItem<Phone> item) {
                item.setOutputMarkupId(true);

                item.setModel(new CompoundPropertyModel<Phone>(item.getModel()));
                item.add(new DropDownChoice<Integer>("threadId", Arrays.asList(1, 2, 3)) {{
                    add(new AjaxFormComponentUpdatingBehavior("onchange") {
                        protected void onUpdate(AjaxRequestTarget target) {
                            Phone phone = item.getModelObject();
                            phone.ext = phone.threadId + "";

                            target.add(item);
                        }
                    });
                }});
                item.add(new TextField("areacode"));
                item.add(new TextField("phone"));
                item.add(new TextField("ext"));

                item.add(new RemoveButton("remove", form));
            }
        };

        form.add(new AjaxFallbackButton("add", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                editor.addItem(new Phone());

                if(target != null) {
                    target.add(form);
                }
            }
        });

        form.add(editor);
    }
}
