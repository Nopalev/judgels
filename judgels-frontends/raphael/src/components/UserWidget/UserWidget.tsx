import { Icon, Menu, MenuDivider, MenuItem, Popover, Position } from '@blueprintjs/core';
import * as React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

import { User } from '../../modules/api/jophiel/user';
import { Profile } from '../../modules/api/jophiel/profile';
import { AppState } from '../../modules/store';
import MenuItemLink from '../MenuItemLink/MenuItemLink';
import { avatarActions as injectedAvatarActions } from '../../routes/jophiel/modules/avatarActions';
import { profileActions as injectedProfileActions } from '../../routes/jophiel/modules/profileActions';

import './UserWidget.css';

export interface UserWidgetProps {
  user?: User;
  onRenderAvatar: (userJid?: string) => Promise<string>;
  onGetProfile: (userJid: string) => Promise<Profile>;
}

interface UserWidgetState {
  avatarUrl?: string;
  profile?: Profile;
}

export class UserWidget extends React.PureComponent<UserWidgetProps, UserWidgetState> {
  state: UserWidgetState = {};

  async componentDidMount() {
    await this.refreshUser();
  }

  async componentDidUpdate(prevProps: UserWidgetProps) {
    if (this.props.user !== prevProps.user) {
      await this.refreshUser();
    }
  }

  render() {
    if (this.props.user) {
      return this.renderForUser(this.props.user);
    } else {
      return this.renderForGuest();
    }
  }

  private refreshUser = async () => {
    let avatarUrl: string;
    let profile: Profile | undefined = undefined;

    const { user, onRenderAvatar, onGetProfile } = this.props;

    if (user) {
      [avatarUrl, profile] = await Promise.all([onRenderAvatar(user.jid), onGetProfile(user.jid)]);
    } else {
      avatarUrl = await onRenderAvatar();
    }
    this.setState({ avatarUrl, profile });
  };

  private renderForUser = (user: User) => {
    const menu = (
      <Menu className="widget-user__menu">
        <MenuItem className="widget-user__menu-helper" icon="user" text={user.username} disabled />
        <MenuDivider className="widget-user__menu-helper" />
        <MenuItemLink text="My profile" to={`/profiles/${user.username}`} />
        <MenuItemLink text="My account" to="/account" />
        <MenuItemLink text="Log out" to="/logout" />
      </Menu>
    );

    const popover = (
      <Popover className="widget-user__avatar-menu" content={menu} position={Position.BOTTOM_RIGHT} usePortal={false}>
        <div>
          <span data-key="username" className="widget-user__user__username">
            {user.username}
          </span>{' '}
          <Icon icon="chevron-down" />
        </div>
      </Popover>
    );

    const responsivePopover = (
      <Popover className="widget-user__burger" content={menu} position={Position.BOTTOM_RIGHT} usePortal={false}>
        <Icon icon="menu" iconSize={Icon.SIZE_LARGE} />
      </Popover>
    );

    return (
      <div className="pt-navbar-group pt-align-right">
        {this.state.avatarUrl && <img src={this.state.avatarUrl} className="widget-user__avatar" />}
        {popover}
        {responsivePopover}
      </div>
    );
  };

  private renderForGuest = () => {
    return (
      <div className="pt-navbar-group pt-align-right">
        <div className="widget-user__link">
          <Link data-key="login" to="/login">
            Log in
          </Link>
        </div>
        <div className="widget-user__link">
          <Link data-key="register" to="/register">
            Register
          </Link>
        </div>
        {this.renderGuestResponsiveMenu()}
      </div>
    );
  };

  private renderGuestResponsiveMenu = () => {
    const menu = (
      <Menu className="widget-user__menu">
        <MenuItemLink text="Log in" to="/login" />
        <MenuItemLink text="Register" to="/register" />
      </Menu>
    );

    return (
      <Popover className="widget-user__burger" content={menu} position={Position.BOTTOM_RIGHT} usePortal={false}>
        <Icon icon="menu" iconSize={Icon.SIZE_LARGE} />
      </Popover>
    );
  };
}

export function createUserWidget(avatarActions, profileActions) {
  const mapStateToProps = (state: AppState) => ({
    user: state.session.user,
  });

  const mapDispatchToProps = {
    onRenderAvatar: avatarActions.renderAvatar,
    onGetProfile: profileActions.getProfile,
  };

  return connect<any>(mapStateToProps, mapDispatchToProps)(UserWidget);
}

export default createUserWidget(injectedAvatarActions, injectedProfileActions);