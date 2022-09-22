import './Footer.scss';

export function Footer() {
  return (
    <div className="footer">
      <hr />
      <small className="footer__text">
        <div className="float-left">&copy; Schetamics NPC 2022</div>
        <div className="float-right">
          Powered by <a href="https://github.com/ia-toki/judgels">Judgels</a>
        </div>
        <div className="clearfix" />
      </small>
    </div>
  );
}
