const GITHUB_API_LATEST_JARVIS_RELEASE =
  "https://api.github.com/repos/scoiatael/Jarvis/releases/latest";

let Listeners = [];
const dispatch = (event, payload) => {
    console.log(event, Listeners);
    Listeners.forEach((listener) => {
      listener(event, payload);
    });
}

const iconFor = (ext) => {
  switch(ext) {
    case "nupkg":
      return "windows";
    case "zip":
      return "apple";
    case "AppImage":
      return "linux";
    case "apk":
      return "android";
    case "deb":
      return "linux";
    case "dmg":
      return "apple";
    case "pacman":
      return "linux";
    case "rpm":
      return "linux";
    case "exe":
      return "windows";
  }
  return "question";
}

const Asset = ({assetData}) => {
  let icon = iconFor(assetData.name.split(".").pop());
  let className = `fa fa-${icon} fa-2x`;
  return (
    <p><i className={className} style={{"padding-right": "0.5em"}}></i><a href={assetData.browser_download_url}>{assetData.name}</a></p>
  )
}

const Release = ({releaseData}) => {
  if(releaseData) {
      let assets = [];
      releaseData.assets.forEach((asset) => {
        assets.push(<Asset assetData={asset} key={asset.id}/>);
      })
      return (
        <div className="inner cover">
          <h1 className="cover-heading">{releaseData.name}</h1>
          <p className="lead"><a href={releaseData.html_url}>{releaseData.tag_name}</a></p>
          {assets}
        </div>
      )
      } else {
      return <i className="fa fa-spinner fa-pulse fa-3x fa-fw"></i>
  }
}

const DownloadLink = () => {
  return (
    <a href="#"
      className="btn btn-lg btn-default"
      onClick={() => { dispatch("download_link_clicked"); }}>
      Download
    </a>
  );
}

const Page = () => {
  return (
    <div className="inner cover">
      <h1 className="cover-heading">Visual Programming Editor - reimagined.</h1>
      <p className="lead">Jarvis is a simple visual Clojure editor.</p>
      <p className="lead">
        <DownloadLink />
      </p>
    </div>
    );
}

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = { show: "page" };
  }

  componentWillMount() {
    $.getJSON(GITHUB_API_LATEST_JARVIS_RELEASE)
      .done((res) => {
        this.setState({ release: res });
      });
    this.listenerPosition = Listeners.push(this.listener.bind(this)) - 1;
  }

  componentWillUnMount() {
    Listeners.splice(this.listenerPosition, 1);
  }

  listener(event, payload) {
    switch(event) {
      case "download_link_clicked":
        this.setState({ show: "download" });
    }
  }

  render() {
    switch(this.state.show) {
      case "page":
        return <Page />;
      case "download":
        return <Release releaseData={this.state.release}/>;
    }
  }
}

ReactDOM.render(
  <App />,
  document.getElementById('content')
);
