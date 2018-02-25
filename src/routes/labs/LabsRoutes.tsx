import * as React from 'react';
import { Route, Switch, withRouter } from 'react-router';

import { Example } from './routes/example/Example/Example';
import ScoreboardContainer from './routes/scoreboard/ScoreboardContainer/ScoreboardContainer';
import ContestDetailPage from './routes/contestDetail/ContestDetailPage/ContestDetailPage';

const LabsRoutes = () => (
  <div>
    <Switch>
      <Route exact path="/labs/example" component={Example} />
      <Route exact path="/labs/scoreboard" component={ScoreboardContainer} />
      <Route exact path="/labs/contestDetail" component={ContestDetailPage} />
    </Switch>
  </div>
);

export default withRouter<any>(LabsRoutes);
