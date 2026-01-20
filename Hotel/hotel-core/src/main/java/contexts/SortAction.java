package contexts;

import enums.SortDirection;

@FunctionalInterface
interface SortAction {

    void execute(SortDirection direction);
}
