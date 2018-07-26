import { push } from 'react-router-redux';

import { changePasswordActions } from './changePasswordActions';
import { BadRequestError } from '../../../../../../modules/api/error';
import { AppState } from '../../../../../../modules/store';
import { sessionState, token } from '../../../../../../fixtures/state';

describe('changePasswordActions', () => {
  let dispatch: jest.Mock<any>;

  const getState = (): Partial<AppState> => ({ session: sessionState });

  let myAPI: jest.Mocked<any>;
  let toastActions: jest.Mocked<any>;

  beforeEach(() => {
    dispatch = jest.fn();

    myAPI = {
      updateMyPassword: jest.fn(),
    };
    toastActions = {
      showSuccessToast: jest.fn(),
      showErrorToast: jest.fn(),
    };
  });

  describe('updateMyPassword()', () => {
    const { updateMyPassword } = changePasswordActions;
    const doUpdateMyPassword = async () =>
      updateMyPassword('oldPass', 'newPass')(dispatch, getState, {
        myAPI,
        toastActions,
      });

    it('tries to change password', async () => {
      await doUpdateMyPassword();

      expect(myAPI.updateMyPassword).toHaveBeenCalledWith(token, {
        oldPassword: 'oldPass',
        newPassword: 'newPass',
      });
    });

    describe('when the old password is correct', () => {
      beforeEach(async () => {
        await doUpdateMyPassword();
      });

      it('succeeds with toast', () => {
        expect(toastActions.showSuccessToast).toHaveBeenCalledWith('Password updated.');
      });

      it('redirects to /profile', () => {
        expect(dispatch).toHaveBeenCalledWith(push('/account/profile'));
      });
    });

    describe('when the old password is incorrect', () => {
      let error: any;

      beforeEach(async () => {
        error = new BadRequestError();
        myAPI.updateMyPassword.mockImplementation(() => {
          throw error;
        });
      });

      it('throws a more descriptive error', async () => {
        await expect(doUpdateMyPassword()).rejects.toEqual(new Error('Incorrect old password.'));
      });
    });
  });
});