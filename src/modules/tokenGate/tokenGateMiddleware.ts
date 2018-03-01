import { tokenGateActions as injectedTokenGateActions } from './tokenGateActions';
import { UnauthorizedError } from '../api/error';

export function createTokenGateMiddleware(tokenGateActions) {
  return store => next => async action => {
    try {
      return await next(action);
    } catch (error) {
      // redirects to /logout if receiving UnauthorizedError
      if (error instanceof UnauthorizedError) {
        return await tokenGateActions.redirectToLogout(next);
      }

      throw error;
    }
  };
}

export const tokenGateMiddleware = createTokenGateMiddleware(injectedTokenGateActions);
