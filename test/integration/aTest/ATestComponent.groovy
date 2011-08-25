package aTest

import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComponentCategory


class ATestComponent extends Component {

    @Override
    protected void doCalculation() {

    }

}

@ComponentCategory(categories=["MyCategory"])
class BTestComponent extends ATestComponent {

}
