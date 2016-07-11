from argparse import ArgumentParser
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import sys

BAR_PLOT = 0
LINE_PLOT = 1


def generate_plot(csv_file_name, plot_file_name, x, hue, y, xticklabels_rotation=90, plot_type=BAR_PLOT):
    """
    Generate plot.
    :param csv_file_name: the input CSV file name
    :param plot_file_name: the output PDF/JPEG file name
    :param x: the x field
    :param hue: the hue field
    :param y: the y field
    :param xticklabels_rotation: the x tick labels rotation
    :param plot_type: the plot type
    :return: None
    """
    sns.set(font_scale=1.5)

    sns.set_style("white", {"legend.frameon": True})

    df = pd.read_csv('../../' + csv_file_name)

    plot_func = sns.barplot if plot_type == BAR_PLOT else sns.pointplot

    ax = plot_func(data=df, x=x, hue=hue, y=y, palette=sns.color_palette("Paired"))
    ax.set_xlabel(x)
    ax.set_ylabel(y)

    labels = ax.get_xticklabels()
    ax.set_xticklabels(labels, rotation=xticklabels_rotation)

    fig = ax.get_figure()

    if hue:
        legend = ax.legend(bbox_to_anchor=(1.05, 1), loc='upper left', borderaxespad=0.)
        legend.set_label('')

        fig.savefig('../../' + plot_file_name, bbox_extra_artists=(legend,), bbox_inches='tight')
        fig.savefig('../../' + plot_file_name + '.jpg', bbox_extra_artists=(legend,), bbox_inches='tight')
    else:
        fig.tight_layout()

        fig.savefig('../../' + plot_file_name)
        fig.savefig('../../' + plot_file_name + '.jpg')

    plt.clf()
    plt.close('all')


if __name__ == '__main__':
    print(sys.argv)

    arg_parser = ArgumentParser()
    arg_parser.add_argument('--csv_file_name', type=str, default=None)
    arg_parser.add_argument('--plot_file_name', type=str, default=None)
    arg_parser.add_argument('--x', type=str, default=None)
    arg_parser.add_argument('--hue', type=str, default=None)
    arg_parser.add_argument('--y', type=str, default=None)

    args = arg_parser.parse_args()

    generate_plot(
        csv_file_name=args.csv_file_name,
        plot_file_name=args.plot_file_name,
        x=args.x,
        hue=args.hue,
        y=args.y,
    )