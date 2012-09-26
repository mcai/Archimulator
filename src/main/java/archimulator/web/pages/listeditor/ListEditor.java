package archimulator.web.pages.listeditor;

import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ListEditor<T> extends RepeatingView implements IFormModelUpdateListener {
    List<T> items;

    public ListEditor(String id, IModel<List<T>> model) {
        super(id, model);
    }

    protected abstract void onPopulateItem(ListItem<T> item);

    public void addItem(T value) {
        items.add(value);
        ListItem<T> item = new ListItem<T>(newChildId(), items.size() - 1);
        add(item);
        onPopulateItem(item);
    }

    @Override
    protected void onBeforeRender() {
        if (!hasBeenRendered()) {
            items = new ArrayList<T>(getModelObject());
            for (int i = 0; i < items.size(); i++) {
                ListItem<T> li = new ListItem<T>(newChildId(), i);
                add(li);
                onPopulateItem(li);
            }
        }
        super.onBeforeRender();
    }

    public void updateModel() {
        setModelObject(items);
    }

    public boolean canRemove(List<T> items, T item) {
        return true;
    }

    @SuppressWarnings("unchecked")
    final boolean checkRemove(ListItem<?> item) {
        List<T> list = Collections.unmodifiableList(items);
        ListItem<T> li = (ListItem<T>) item;
        return canRemove(list, li.getModelObject());
    }

    @SuppressWarnings("unchecked")
    public final IModel<List<T>> getModel() {
        return (IModel<List<T>>) getDefaultModel();
    }

    public final void setModel(IModel<List<T>> model) {
        setDefaultModel(model);
    }

    @SuppressWarnings("unchecked")
    public final List<T> getModelObject() {
        return (List<T>) getDefaultModelObject();
    }

    public final void setModelObject(List<T> object) {
        setDefaultModelObject(object);
    }
}
