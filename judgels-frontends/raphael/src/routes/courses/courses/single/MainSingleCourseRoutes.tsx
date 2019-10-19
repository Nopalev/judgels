import * as React from 'react';
import { Route, withRouter } from 'react-router';

import SingleCourseDataRoute from './SingleCourseDataRoute';
import SingleCourseRoutes from './SingleCourseRoutes';

const MainSingleCourseRoutes = () => (
  <div>
    <Route path="/courses/:courseSlug" component={SingleCourseDataRoute} />
    <Route path="/courses/:courseSlug" component={SingleCourseRoutes} />
  </div>
);

export default withRouter<any, any>(MainSingleCourseRoutes);