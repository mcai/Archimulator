package archimulator.web.pages.listeditor;

import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;

import java.util.List;

public abstract class EditorButton extends AjaxFallbackButton {
    protected Form<?> form;
    private transient ListItem<?> parent;

    public EditorButton(String id, Form<?> form) {
        super(id, form);
        this.form = form;
    }

    protected final ListItem<?> getItem() {
        if (parent == null) {
            parent = findParent(ListItem.class);
        }
        return parent;
    }

    protected final List<?> getList() {
        return getEditor().items;
    }

    protected final ListEditor<?> getEditor() {
        return (ListEditor<?>) getItem().getParent();
    }

    @Override
    protected void onDetach() {
        parent = null;
        super.onDetach();
    }
}