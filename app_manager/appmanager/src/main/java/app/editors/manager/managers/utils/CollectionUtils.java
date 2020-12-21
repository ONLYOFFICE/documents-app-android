package app.editors.manager.managers.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.user.Group;
import app.editors.manager.mvp.models.user.User;

public class CollectionUtils {

    public static abstract class CollectionTransformer<A, B> {

        abstract B transform(A a);

        public List<B> transform(List<A> list) {
            final List<B> newList = new ArrayList<>();
            for (A a : list) {
                newList.add(transform(a));
            }
            return newList;
        }

        public List<B> transform(Set<A> list) {
            final List<B> newList = new ArrayList<>();
            for (A a : list) {
                newList.add(transform(a));
            }
            return newList;
        }
    }

    public static List<Entity> convertUsersToItems(List<User> list) {

        CollectionTransformer transformer = new CollectionTransformer<User, Entity>() {
            @Override
            Entity transform(User e) {
                return e;
            }
        };
        return transformer.transform(list);
    }

    public static List<Entity> convertGroupsToItems(List<Group> list) {

        CollectionTransformer transformer = new CollectionTransformer<Group, Entity>() {
            @Override
            Entity transform(Group e) {
                return e;
            }
        };
        return transformer.transform(list);
    }

    public static List<Entity> convertUsersToItems(Set<User> list) {

        CollectionTransformer transformer = new CollectionTransformer<User, Entity>() {
            @Override
            Entity transform(User e) {
                return e;
            }
        };
        return transformer.transform(list);
    }

    public static List<Entity> convertGroupsToItems(Set<Group> list) {

        CollectionTransformer transformer = new CollectionTransformer<Group, Entity>() {
            @Override
            Entity transform(Group e) {
                return e;
            }
        };
        return transformer.transform(list);
    }


}
