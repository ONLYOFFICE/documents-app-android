package app.editors.manager.mvp.models.base;

public enum  AppContext {

    NONE(0),
    MY(1),
    SHARE(2),
    COMMON(3),
    PROJECTS(4),
    TRASH(5);

    private final int mValue;

    AppContext(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public static AppContext  getEnum(int value){
        for (AppContext e : AppContext.values()) {
            if(e.getValue() == value)
                return e;
        }
        return AppContext.NONE;
    }

}
